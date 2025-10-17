package vn.cineshow.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.cineshow.enums.RoomStatus;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Room extends AbstractEntity implements Serializable {

    String name;

    @ManyToOne(fetch = FetchType.LAZY)
    RoomType roomType;

    @Enumerated(EnumType.STRING)   // <-- add this for enums
    RoomStatus status;

    @OneToMany(mappedBy = "room",  cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    List<Seat> seats;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    List<ShowTime> shows;

    @Column(name = "rows_count")
    Integer rows;                    // số hàng ghế

    @Column(name = "columns_count")
    Integer columns;                 // số cột ghế

    @Column
    Integer capacity;                // = rows * columns (cập nhật ở service)

    @Column(length = 500)
    String description;              // mô tả phòng (optional)

    @Column(length = 100)
    String screenType;               // loại màn hình (optional)

}
