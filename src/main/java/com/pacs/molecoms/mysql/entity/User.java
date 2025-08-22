package com.pacs.molecoms.mysql.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"email", "provider"})
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id @GeneratedValue
    private Long id;

    private String email;

    private String password;

    private String nickname;

    private String provider;

    // ✅ ChatRoom 연관관계 추가
//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<ChatRoom> chatRooms = new ArrayList<>();
//
//    public void changePassword(String encodedPassword) {
//        this.password = encodedPassword;
//    }
//
//    public void changeNickname(String newNickname) {
//        this.nickname = newNickname;
//    }

}
