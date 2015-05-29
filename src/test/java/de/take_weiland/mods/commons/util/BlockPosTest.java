package de.take_weiland.mods.commons.util;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author diesieben07
 */

public class BlockPosTest {

    @Test
    public void testLongEncoding() {
        long l = BlockPos.toLong(120, 130, 160);
        BlockPos pos = BlockPos.fromLong(l);
        assertThat(pos.x(), is(equalTo(120)));
        assertThat(pos.y(), is(equalTo(130)));
        assertThat(pos.z(), is(equalTo(160)));
    }

    @Test
    public void testLongEncodingNeg() {
        long l = BlockPos.toLong(-120, 17, -3000);
        BlockPos pos = BlockPos.fromLong(l);
        assertThat(pos.x(), is(equalTo(-120)));
        assertThat(pos.y(), is(equalTo(17)));
        assertThat(pos.z(), is(equalTo(-3000)));
    }

    @Test
    public void testEquals() {
        assertThat(new BlockPos(1, 2, 3), is(equalTo(new BlockPos(1, 2, 3))));
    }

    @Test
    public void testHashCode() {
        assertThat(new BlockPos(1, 2, 3).hashCode(), is(equalTo(new BlockPos(1, 2, 3).hashCode())));
    }

}
