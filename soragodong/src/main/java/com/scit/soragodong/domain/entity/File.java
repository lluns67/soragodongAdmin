package com.scit.soragodong.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "FILE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class File{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FILE_IDX")
    private Integer fileIdx;


    @Column(name = "FILE_NAME")
    private String fileName;

    @Column(name = "ORIGINAL_NAME")
    private String originalName;

    @Column(name = "FILE_EXT")
    private String fileExt;

    @Column(name = "FILE_SIZE")
    private Integer fileSize;

    @Column(name = "FILE_PATH")
    private String filePath;

    @Column(name = "FILE_ORDER")
    private Integer fileOrder;

    @Builder.Default
    @Column(name = "IS_USE", nullable = false)
    private Boolean isUse = true;

}
