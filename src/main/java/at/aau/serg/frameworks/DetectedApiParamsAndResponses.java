package at.aau.serg.frameworks;

import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class DetectedApiParamsAndResponses {

    private List<Parameter> detectedApiParameters;

    private ApiResponses detectedApiResponses;

    @Override
    public String toString() {
        return detectedApiParameters.toString() + '\n' + detectedApiResponses.toString();
    }

    public void addAllDetected(DetectedApiParamsAndResponses previouslyDetectedApiParamsAndResponses) {
        if (previouslyDetectedApiParamsAndResponses != null) {
            detectedApiParameters.addAll(previouslyDetectedApiParamsAndResponses.detectedApiParameters);
            detectedApiResponses.putAll(previouslyDetectedApiParamsAndResponses.detectedApiResponses);
        }
    }
}
