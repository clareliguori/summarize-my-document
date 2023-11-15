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
public class WordCloudRequest {
    /** The content of the document that will be analyzed to generate the word cloud */
    @JsonProperty("doc_content")
    private String docContent;
}
