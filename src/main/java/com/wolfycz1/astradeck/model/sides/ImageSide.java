package com.wolfycz1.astradeck.model.sides;

import com.wolfycz1.astradeck.model.Media;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ImageSide extends TextSide {
    private Media image;
}
