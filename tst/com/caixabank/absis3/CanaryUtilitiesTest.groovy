package com.caixabank.absis3

import org.junit.Test

import static org.junit.Assert.assertEquals

class CanaryUtilitiesTest extends GroovyTestCase {

    @Test
    void testCanaryPercentagesAreAsExpected() {
        assertTrue(CanaryUtilities.CANARY_ROLLOUT_PERCENTAGES == [0, 5, 10, 25, 50, 100])
    }

    @Test
    void testCanaryInitPercentageAreAsExpected() {
        assertEquals(CanaryUtilities.initialPercentage(), 0)
    }

    @Test
    void testCanaryFinalPercentageIsAsExpected() {
        assertEquals(CanaryUtilities.finalPercentage(), 50)
    }

    @Test
    void testCanaryFinalPercentageIsAsExpectedWhenMatchesWithAllPercentagesList() {
        assertEquals(CanaryUtilities.finalPercentage("service-manager-micro-1"), 100)
    }

    @Test
    void testCanaryIncrementPercentageFromZero_returnsFive() {
        assertEquals(CanaryUtilities.incrementPercentage(0), 5)
    }

    @Test
    void testCanaryIncrementPercentageFromFifty_returnsOneHundred() {
        assertEquals(CanaryUtilities.incrementPercentage(50), 100)
    }

    @Test
    void testCanaryIncrementPercentageFromOneHundred_returnsNothing() {
        assertEquals(CanaryUtilities.incrementPercentage(100), null)
    }

    @Test
    void testCanaryCheckIfNextIncreaseFromZeroIsNotFinal() {
        assertFalse(CanaryUtilities.weHaveReachedFinalPercentage(0))
    }

    @Test
    void testCanaryCheckIfNextIncreaseFromTwentyFiveIsNotFinal() {
        assertFalse(CanaryUtilities.weHaveReachedFinalPercentage(25))
    }

    @Test
    void testCanaryCheckIfNextIncreaseFromFiftyIsFinal() {
        assertTrue(CanaryUtilities.weHaveReachedFinalPercentage(50))
    }

    @Test
    void testCanaryCheckIfNextIncreaseFromOneHundredIsFinal() {
        assertTrue(CanaryUtilities.weHaveReachedFinalPercentage(100))
    }

    @Test
    void testCanaryCheckIfNextIncreaseFromOneHundredAndOneIsFinal() {
        assertTrue(CanaryUtilities.weHaveReachedFinalPercentage(101))
    }

    @Test
    void testCanaryCheckIfNextIncreaseFromZeroIsNotFinalWhenArtifactMatchesAllPercentagesList() {
        assertFalse(CanaryUtilities.weHaveReachedFinalPercentage(0, "service-manager-micro-1"))
    }

    @Test
    void testCanaryCheckIfNextIncreaseFromTwentyFiveIsNotFinalWhenArtifactMatchesAllPercentagesList() {
        assertFalse(CanaryUtilities.weHaveReachedFinalPercentage(25, "service-manager-micro-1"))
    }

    @Test
    void testCanaryCheckIfNextIncreaseFromFiftyIsFinalWhenArtifactMatchesAllPercentagesList() {
        assertFalse(CanaryUtilities.weHaveReachedFinalPercentage(50, "service-manager-micro-1"))
    }

    @Test
    void testCanaryCheckIfNextIncreaseFromOneHundredIsFinalWhenArtifactMatchesAllPercentagesList() {
        assertTrue(CanaryUtilities.weHaveReachedFinalPercentage(100, "service-manager-micro-1"))
    }

    @Test
    void testCanaryCheckIfNextIncreaseFromOneHundredAndOneIsFinalWhenArtifactMatchesAllPercentagesList() {
        assertTrue(CanaryUtilities.weHaveReachedFinalPercentage(101, "service-manager-micro-1"))
    }
    
}
