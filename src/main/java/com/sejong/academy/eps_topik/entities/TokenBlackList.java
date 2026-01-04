/**
 * Created By Dilsha Prasanna
 * Date : 1/10/2024
 * Time : 1:16 PM
 * Project Name : upay-sso
 */

package com.sejong.academy.eps_topik.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenBlackList {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "token_black_list_sequence")
    @SequenceGenerator(name = "token_black_list_sequence", sequenceName = "token_black_list_sequence", allocationSize = 1)
    private Long id;
    @Column(length = 1000)
    private String token;
    private LocalDateTime expiredTime;
}
