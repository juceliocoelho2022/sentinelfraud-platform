package br.com.jucelio.sentinelfraud.rules;

import br.com.jucelio.sentinelfraud.domain.TransactionContext;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;
import static org.assertj.core.api.Assertions.assertThat;

class RulesTest {
    private TransactionContext tx(BigDecimal amount,String country,String instant) {
        return new TransactionContext("tx-1","c-1",amount,"BRL","d-1","127.0.0.1",country,Instant.parse(instant));
    }
    @Test void shouldScoreHighAmount() { assertThat(new HighAmountRule(new BigDecimal("10000")).evaluate(tx(new BigDecimal("15000"),"BR","2026-09-09T12:00:00Z")).score()).isEqualTo(45); }
    @Test void shouldScoreForeignCountry() { assertThat(new ForeignTransactionRule().evaluate(tx(BigDecimal.TEN,"US","2026-09-09T12:00:00Z")).reason()).isEqualTo("FOREIGN_COUNTRY"); }
    @Test void shouldScoreSuspiciousHour() { assertThat(new SuspiciousHourRule().evaluate(tx(BigDecimal.TEN,"BR","2026-09-09T02:00:00Z")).matched()).isTrue(); }
}
