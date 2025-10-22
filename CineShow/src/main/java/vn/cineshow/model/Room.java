package vn.cineshow.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.cineshow.enums.RoomStatus;
import java.io.Serializable;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import vn.cineshow.enums.RoomStatus;

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

    @Enumerated(EnumType.STRING)
    RoomStatus status;

    @OneToMany(mappedBy = "room",  cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    List<Seat> seats;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    List<ShowTime> shows;

    @Column(name = "rows_count")
    Integer rows;

    @Column(name = "columns_count")
    Integer columns;

    @Column
    Integer capacity;

    @Column(length = 500)
    String description;

    @Column(length = 100)
    String screenType;
}
