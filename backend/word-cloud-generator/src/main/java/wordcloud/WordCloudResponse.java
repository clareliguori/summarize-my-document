package wordcloud;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WordCloudResponse {
    /** The URL of the generated word cloud image */
    @JsonProperty("word_cloud_url")
    private String wordCloudUrl;
}
