package tech.neatnet.core.rule.engine.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.neatnet.core.rule.engine.domain.Rule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RuleCacheImplTest {

    @Mock
    private RuleRepository ruleRepository;

    @InjectMocks
    private RuleCacheImpl ruleCache;

    @Test
    void getAllRuleMatrices_ShouldCallRepository() {
        when(ruleRepository.findAll()).thenReturn(
                Arrays.asList(Rule.builder().build(), Rule.builder().build()));

        Collection<Rule> matrices = ruleCache.getAllRules();

        verify(ruleRepository).findAll();
        assertEquals(2, matrices.size());
    }

    @Test
    void reloadRuleMatrices_ShouldInvokeRepositoryFindAll() {
        when(ruleRepository.findAll()).thenReturn(Collections.emptyList());

        ruleCache.reloadRules();

        verify(ruleRepository).findAll();
    }

}