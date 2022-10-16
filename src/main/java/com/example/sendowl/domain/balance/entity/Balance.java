package com.example.sendowl.domain.balance.entity;

import com.example.sendowl.common.entity.BaseEntity;
import lombok.*;
import javax.persistence.*;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Balance extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    private String title;
    private String firstDetail;
    private String secondDetail;

    @Override
    public String toString() {
        return "Balance{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", firstDetail='" + firstDetail + '\'' +
                ", secondDetail='" + secondDetail + '\'' +
                '}';
    }
}
