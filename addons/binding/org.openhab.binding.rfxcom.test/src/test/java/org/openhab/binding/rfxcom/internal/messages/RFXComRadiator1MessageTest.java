package org.openhab.binding.rfxcom.internal.messages;

import static org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage.PacketType.RADIATOR1;

import org.junit.Test;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComMessageNotImplementedException;

public class RFXComRadiator1MessageTest {
    @Test(expected = RFXComMessageNotImplementedException.class)
    public void checkNotImplemented() throws Exception {
        RFXComMessageFactory.createMessage(RADIATOR1);
    }
}
