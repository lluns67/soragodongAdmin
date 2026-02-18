package com.scit.soragodong.domain.dto;

import com.scit.soragodong.domain.enums.FileRefType;
import org.hibernate.id.IntegralDataTypeHolder;

public record FileUploadRequest(
                FileRefType refType,
                IntegralDataTypeHolder refId) {
}
