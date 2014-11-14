package de.take_weiland.mods.commons.net;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.lang.annotation.ElementType;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author diesieben07
 */
@RunWith(Parameterized.class)
public class EnumSetHandlerTest {

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(
                new Object[]{new BufferUtils.EnumSetHandlerPureJava()},
                new Object[]{new BufferUtils.EnumSetHandlerFast()}
        );
    }

    private final BufferUtils.EnumSetHandler handler;

    public EnumSetHandlerTest(BufferUtils.EnumSetHandler handler) {
        this.handler = handler;
    }

    @Test
    public void testAsLong() {
        EnumSet<ElementType> es = EnumSet.of(ElementType.TYPE, ElementType.METHOD);
        long enc = (1 << ElementType.TYPE.ordinal()) | (1 << ElementType.METHOD.ordinal());

        assertThat(handler.asLong(es), is(equalTo(enc)));
    }

    @Test
    public void testCreateShared() {
        long enc = (1 << ElementType.TYPE.ordinal()) | (1 << ElementType.METHOD.ordinal());
        assertThat(handler.createShared(ElementType.class, enc), is(equalTo(EnumSet.of(ElementType.TYPE, ElementType.METHOD))));
    }

    @Test
    public void testUpdateInPlace1() {
        long enc = (1 << ElementType.TYPE.ordinal()) | (1 << ElementType.METHOD.ordinal());
        assertThat(handler.update(ElementType.class, EnumSet.noneOf(ElementType.class), enc), is(equalTo(EnumSet.of(ElementType.TYPE, ElementType.METHOD))));
    }

    @Test
    public void testUpdateInPlace2() {
        long enc = (1 << ElementType.TYPE.ordinal()) | (1 << ElementType.METHOD.ordinal());
        assertThat(handler.update(ElementType.class, EnumSet.allOf(ElementType.class), enc), is(equalTo(EnumSet.of(ElementType.TYPE, ElementType.METHOD))));
    }

    @Test
    public void testUpdateInPlace3() {
        long enc = (1 << ElementType.TYPE.ordinal()) | (1 << ElementType.METHOD.ordinal());
        assertThat(handler.update(ElementType.class, null, enc), is(equalTo(EnumSet.of(ElementType.TYPE, ElementType.METHOD))));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTooBig1() {
        new BufferUtils.EnumSetHandlerPureJava().createShared(TestEnumBig.class, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTooBig2() {
        new BufferUtils.EnumSetHandlerPureJava().update(TestEnumBig.class, null, 0);
    }

    enum TestEnumBig {
        A0, B0, C0, D0, E0, F0, G0, H0, I0, J0, K0, L0, M0, N0, O0, P0, Q0, R0, S0, T0, U0, V0, W0, X0, Y0, Z0,
        A1, B1, C1, D1, E1, F1, G1, H1, I1, J1, K1, L1, M1, N1, O1, P1, Q1, R1, S1, T1, U1, V1, W1, X1, Y1, Z1,
        A2, B2, C2, D2, E2, F2, G2, H2, I2, J2, K2, L2, M2, N2, O2, P2, Q2, R2, S2, T2, U2, V2, W2, X2, Y2, Z2
    }

}
