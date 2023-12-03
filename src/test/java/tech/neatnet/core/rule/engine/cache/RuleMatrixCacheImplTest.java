package tech.neatnet.core.rule.engine.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.neatnet.core.rule.engine.domain.RuleMatrix;
import tech.neatnet.core.rule.engine.repositories.RuleMatrixRepository;

@ExtendWith(MockitoExtension.class)
class RuleMatrixCacheImplTest {

  @Mock
  private RuleMatrixRepository ruleMatrixRepository;

  @InjectMocks
  private RuleMatrixCacheImpl ruleMatrixCache;

  @Test
  void getAllRuleMatrices_ShouldCallRepository() {
    when(ruleMatrixRepository.findAll()).thenReturn(
        Arrays.asList(new RuleMatrix(), new RuleMatrix()));

    Collection<RuleMatrix> matrices = ruleMatrixCache.getAllRuleMatrices();

    verify(ruleMatrixRepository).findAll();
    assertEquals(2, matrices.size());
  }

  @Test
  void reloadRuleMatrices_ShouldInvokeRepositoryFindAll() {
    when(ruleMatrixRepository.findAll()).thenReturn(Collections.emptyList());

    ruleMatrixCache.reloadRuleMatrices();

    verify(ruleMatrixRepository).findAll();
  }

}