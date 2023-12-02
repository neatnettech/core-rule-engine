package tech.neatnet.core.rule.engine.domain;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rule implements Serializable {

  @Transient
  private static final long serialVersionUID = 1L;
  private String condition;
  private String action;
  private List<Object> inValues;

}
