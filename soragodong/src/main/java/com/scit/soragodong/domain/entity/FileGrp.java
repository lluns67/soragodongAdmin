package com.scit.soragodong.domain.entity;

import com.scit.soragodong.domain.enums.FileRefType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "FILE_GRP")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FileGrp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FILE_GRP_IDX")
    private Integer fileGrpIdx;

    @Enumerated(EnumType.STRING)
    @Column(name = "REF_TYPE", nullable = false)
    private FileRefType refType;

    @Column(name = "REF_IDX", nullable = false)
    private Integer refId;

    @CreatedDate
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;



    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

}
