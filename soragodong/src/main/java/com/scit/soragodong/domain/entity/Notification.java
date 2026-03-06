package com.scit.soragodong.domain.entity;

import com.scit.soragodong.domain.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "NOTIFICATION")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NOTI_IDX")
    private Integer notiIdx;

    @Column(name = "USER_IDX", nullable = false)
    private Integer userIdx;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "NOTI_TYPE",  nullable = false)
	private NotificationType notiType;

    @Column(name = "REF_ID")
    private Integer refId;

    @Column(name = "MESSAGE", length = 500)
    private String message;

    @Column(name = "IS_READ", nullable = false)
    private Boolean isRead = false;

    @Column(name = "CREATED_AT", updatable = false,
            columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
	
	@Builder.Default
	@Column(name = "IS_USE")
	private Boolean isUse = true;

}
