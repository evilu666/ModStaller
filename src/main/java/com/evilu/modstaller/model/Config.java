package com.evilu.modstaller.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Config
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Config {

    @Builder.Default
    private Settings settings = new Settings();
    
}
