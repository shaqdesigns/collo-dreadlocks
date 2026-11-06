package com.collo.dreadlocks.remark;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RemarkCreateRequest(
        @NotBlank(message = "remark is required")
        @Size(max = 1000, message = "remark must be at most 1000 characters")
        String body
) {
}
