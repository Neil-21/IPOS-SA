package com.infopharma.ipos_sa.config;

/**
 * MapperConfig
 * Spring {@code @Configuration} that exposes a singleton {@link org.modelmapper.ModelMapper}
 * bean configured with {@code STRICT} matching strategy, ensuring only
 * explicitly matched fields are mapped and preventing accidental field
 * collisions across different DTO types.
 */
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)  // only map exact field name matches
                .setSkipNullEnabled(true);                        // skip null source fields (safe partial updates)
        return modelMapper;
    }
}