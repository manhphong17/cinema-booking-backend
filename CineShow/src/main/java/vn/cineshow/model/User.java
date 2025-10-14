package vn.cineshow.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.cineshow.enums.Gender;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User extends AbstractEntity implements Serializable {


    String name;

    String address;

    int loyalPoint;

    LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    Gender gender;

    @OneToOne()
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    Account account;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VoucherItem> voucherItems;

}