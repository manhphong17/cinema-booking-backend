package vn.cineshow.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.cineshow.dto.request.seat.*;
import vn.cineshow.dto.response.room.RoomDTO;
import vn.cineshow.dto.response.room.room_type.RoomTypeDTO;
import vn.cineshow.dto.response.seat.SeatCellDTO;
import vn.cineshow.dto.response.seat.SeatMatrixResponse;
import vn.cineshow.dto.response.seat.seat_type.SeatTypeDTO;
import vn.cineshow.enums.SeatStatus;
import vn.cineshow.model.Room;
import vn.cineshow.model.RoomType;
import vn.cineshow.model.Seat;
import vn.cineshow.model.SeatType;
import vn.cineshow.repository.RoomRepository;
import vn.cineshow.repository.SeatRepository;
import vn.cineshow.repository.SeatTypeRepository;
import vn.cineshow.service.SeatService;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeatServiceImpl implements SeatService {

    private final RoomRepository roomRepository;
    private final SeatRepository seatRepository;
    private final SeatTypeRepository seatTypeRepository;

    /* =========================
       1) INIT
       ========================= */

    @Override
    @Transactional
    public int initSeats(Long roomId, SeatInitRequest request) {
        Room room = roomRepository.findById(roomId).orElse(null);
        if (room == null) return 0;

        seatRepository.deleteByRoom_Id(roomId);
        seatRepository.flush();

        SeatType defaultType = seatTypeRepository.findById(request.getDefaultSeatTypeId())
                .orElseThrow(() -> new IllegalArgumentException("SeatType không tồn tại"));

        int rows = request.getRows();
        int cols = request.getColumns();

        List<Seat> batch = new ArrayList<>(rows * cols);
        for (int r = 1; r <= rows; r++) {
            for (int c = 1; c <= cols; c++) {
                Seat s = new Seat();
                s.setRoom(room);
                s.setRow(String.valueOf(r));
                s.setColumn(String.valueOf(c));
                s.setSeatType(defaultType);
                s.setStatus(SeatStatus.AVAILABLE);
                batch.add(s);
            }
        }
        seatRepository.saveAll(batch);

        room.setRows(rows);
        room.setColumns(cols);
        room.setCapacity(rows * cols);
        roomRepository.save(room);

        return batch.size();
    }

    /* =========================
       2) GET MATRIX
       ========================= */
    @Override
    @Transactional(readOnly = true)
    public SeatMatrixResponse getSeatMatrix(Long roomId) {
        Room room = roomRepository.findById(roomId).orElse(null);
        if (room == null) return null;

        // Lấy ghế theo row/column (String) đã sắp xếp
        List<Seat> seats = seatRepository.findByRoom_IdOrderByRowAscColumnAsc(roomId);

        int rows = room.getRows() != null ? room.getRows() : 0;
        int cols = room.getColumns() != null ? room.getColumns() : 0;

        List<List<SeatCellDTO>> matrix = new ArrayList<>(rows);
        for (int r = 0; r < rows; r++) {
            matrix.add(new ArrayList<>(Collections.nCopies(cols, null)));
        }

        for (Seat s : seats) {
            // parse row/column (String) -> index 1-based
            int r, c;
            try {
                r = Integer.parseInt(s.getRow());
                c = Integer.parseInt(s.getColumn());
            } catch (NumberFormatException e) {
                continue; // bỏ qua ghế có dữ liệu row/column không hợp lệ
            }
            if (r <= 0 || c <= 0 || r > rows || c > cols) continue;

            SeatType st = s.getSeatType();
            SeatTypeDTO seatTypeDTO = null;
            if (s.getStatus() == SeatStatus.BLOCKED) {
                seatTypeDTO = SeatTypeDTO.builder().id(-1L).build();
            } else if (st != null) {
                seatTypeDTO = SeatTypeDTO.builder()
                        .id(st.getId())
                        .name(st.getName())
                        .description(st.getDescription())
                        .build();
            }

            String rowLabel = toRowLabel(r);
            SeatCellDTO cell = SeatCellDTO.builder()
                    .id(s.getId())
                    .rowIndex(r)
                    .columnIndex(c)
                    .rowLabel(rowLabel)
                    .number(c)
                    .seatType(seatTypeDTO)
                    .status(s.getStatus() == null ? null : s.getStatus().name())
                    .isBlocked(s.getStatus() == SeatStatus.BLOCKED)
                    .note(null)
                    .build();

            matrix.get(r - 1).set(c - 1, cell);
        }

        return SeatMatrixResponse.builder()
                .room(toRoomDTO(room))
                .matrix(matrix)
                .build();
    }

    /* =========================
       3) SAVE MATRIX (UPSERT)
       ========================= */
    @Override
    @Transactional
    public Map<String, Integer> saveSeatMatrix(Long roomId, SeatMatrixRequest request) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room không tồn tại"));

        List<Seat> existing = seatRepository.findByRoom_Id(roomId);
        Map<String, Seat> existingMap = existing.stream()
                .collect(Collectors.toMap(
                        s -> key(s.getRow(), s.getColumn()), // dùng row/column String
                        s -> s
                ));

        int created = 0, updated = 0;

        int newRows = request.getMatrix() != null ? request.getMatrix().size() : 0;
        int newCols = (request.getMatrix() == null) ? 0
                : request.getMatrix().stream().mapToInt(row -> row == null ? 0 : row.size()).max().orElse(0);

        if (request.getMatrix() != null) {
            for (List<SeatCellRequest> rowList : request.getMatrix()) {
                if (rowList == null) continue;
                for (SeatCellRequest cellReq : rowList) {
                    if (cellReq == null) continue;

                    int r = cellReq.getRowIndex();
                    int c = cellReq.getColumnIndex();
                    String k = key(String.valueOf(r), String.valueOf(c));
                    Seat found = existingMap.get(k);

                    SeatType type = null;
                    if (cellReq.getSeatTypeId() != -1) {
                        type = seatTypeRepository.findById(cellReq.getSeatTypeId())
                                .orElseThrow(() -> new IllegalArgumentException("SeatType không tồn tại: " + cellReq.getSeatTypeId()));
                    }

                    // String -> Enum (mặc định ACTIVE)
                    SeatStatus desiredStatus = SeatStatus.AVAILABLE;
                    if (cellReq.getSeatTypeId() == -1) {
                        desiredStatus = SeatStatus.BLOCKED;
                    } else if (cellReq.getStatus() != null && !cellReq.getStatus().isBlank()) {
                        try {
                            desiredStatus = SeatStatus.valueOf(cellReq.getStatus().trim().toUpperCase());
                        } catch (IllegalArgumentException ignored) { /* giữ ACTIVE */ }
                    }

                    String rowLabel = toRowLabel(r);
                    if (found == null) {
                        Seat s = new Seat();
                        s.setRoom(room);
                        s.setRow(String.valueOf(r));
                        s.setColumn(String.valueOf(c));
                        s.setSeatType(type);
                        s.setStatus(desiredStatus);

                        seatRepository.save(s);
                        created++;
                    } else {
                        boolean changed = false;

                        if (type != null && (found.getSeatType() == null || !found.getSeatType().getId().equals(type.getId()))) {
                            found.setSeatType(type);
                            changed = true;
                        }
                        if (found.getStatus() != desiredStatus) {
                            found.setStatus(desiredStatus);
                            changed = true;
                        }

                        if (changed) {
                            seatRepository.save(found);
                            updated++;
                        }
                        existingMap.remove(k);
                    }
                }
            }
        }

        // Xóa ghế không còn trong payload
        int deleted = 0;
        for (Seat toDel : existingMap.values()) {
            seatRepository.delete(toDel);
            deleted++;
        }

        // Đồng bộ kích thước room
        if (!Objects.equals(room.getRows(), newRows) || !Objects.equals(room.getColumns(), newCols)) {
            room.setRows(newRows);
            room.setColumns(newCols);
            room.setCapacity(newRows * newCols);
            roomRepository.save(room);
        }

        Map<String, Integer> result = new HashMap<>();
        result.put("updated", updated);
        result.put("created", created);
        result.put("deleted", deleted);
        return result;
    }

    /* =========================
       4) BULK TYPE
       ========================= */
    @Override
    @Transactional
    public int bulkUpdateSeatType(Long roomId, BulkTypeRequest request) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room không tồn tại"));
        SeatType type = seatTypeRepository.findById(request.getSeatTypeId())
                .orElseThrow(() -> new IllegalArgumentException("SeatType không tồn tại"));

        List<Seat> toSave = new ArrayList<>();
        for (SeatPosition pos : request.getTargets()) {
            seatRepository.findByRoom_IdAndRowAndColumn(room.getId(), String.valueOf(pos.getRowIndex()), String.valueOf(pos.getColumnIndex()))
                    .ifPresent(seat -> {
                        seat.setSeatType(type);
                        toSave.add(seat);
                    });
        }
        if (!toSave.isEmpty()) seatRepository.saveAll(toSave);
        return toSave.size();
    }

    /* =========================
       5) BULK BLOCK
       ========================= */
    @Override
    @Transactional
    public int bulkBlockSeats(Long roomId, BulkBlockRequest request) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room không tồn tại"));

        boolean block = Boolean.TRUE.equals(request.getBlocked());
        List<Seat> toSave = new ArrayList<>();

        for (SeatPosition pos : request.getTargets()) {
            seatRepository.findByRoom_IdAndRowAndColumn(room.getId(),
                            String.valueOf(pos.getRowIndex()),
                            String.valueOf(pos.getColumnIndex()))
                    .ifPresent(seat -> {
                        // Sync status theo blocked
                        if (block) seat.setStatus(SeatStatus.BLOCKED);
                        else if (seat.getStatus() == SeatStatus.BLOCKED) seat.setStatus(SeatStatus.AVAILABLE);
                        toSave.add(seat);
                    });
        }

        if (!toSave.isEmpty()) seatRepository.saveAll(toSave);
        return toSave.size();
    }

    /* =========================
       Helpers
       ========================= */

    private String key(String row, String column) {
        return row + "," + column;
    }

    /**
     * 1->A, 26->Z, 27->AA, ...
     */
    private String toRowLabel(int index1Based) {
        StringBuilder sb = new StringBuilder();
        int n = index1Based;
        while (n > 0) {
            int rem = (n - 1) % 26;
            sb.insert(0, (char) ('A' + rem));
            n = (n - 1) / 26;
        }
        return sb.toString();
    }

    private RoomDTO toRoomDTO(Room room) {
        RoomType rt = room.getRoomType();
        RoomTypeDTO rtDTO = null;
        if (rt != null) {
            rtDTO = RoomTypeDTO.builder()
                    .id(rt.getId())
//                    .code(rt.getCode())
                    .name(rt.getName())
                    .description(rt.getDescription())
                    .build();
        }
        return RoomDTO.builder()
                .id(room.getId())
                .name(room.getName())
                .roomType(rtDTO)
                .rows(room.getRows())
                .columns(room.getColumns())
                .capacity(room.getCapacity())
                .status(room.getStatus() == null ? null : room.getStatus().name()) // enum -> String
                .description(room.getDescription())
                .createdAt(room.getCreatedAt())
                .updatedAt(room.getUpdatedAt())
                .build();
    }
}
