/*
This file is part of jpcsp.

Jpcsp is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Jpcsp is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Jpcsp.  If not, see <http://www.gnu.org/licenses/>.
 */
package jpcsp.memory.mmio;

import static jpcsp.MemoryMap.END_IO_1;
import static jpcsp.MemoryMap.START_IO_0;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import jpcsp.Memory;
import jpcsp.MemoryMap;
import jpcsp.hardware.Screen;

public class MMIO extends Memory {
    private final Memory mem;
    private final Map<Integer, IMMIOHandler> handlers = new HashMap<Integer, IMMIOHandler>();
    protected static final boolean[] validMemoryPage = new boolean[Memory.validMemoryPage.length];

    public MMIO(Memory mem) {
    	this.mem = mem;
    }

    @Override
    public boolean allocate() {
    	System.arraycopy(Memory.validMemoryPage, 0, validMemoryPage, 0, validMemoryPage.length);
    	Arrays.fill(validMemoryPage, START_IO_0 >>> MEMORY_PAGE_SHIFT, (MemoryMap.END_EXCEPTIO_VEC >>> MEMORY_PAGE_SHIFT) + 1, true);

        return true;
    }

    @Override
	public void Initialise() {
    	handlers.clear();

    	addHandlerRW(0xBC000000, 0x54); // Memory interface
    	addHandler(MMIOHandlerSystemControl.BASE_ADDRESS, MMIOHandlerSystemControl.SIZE_OF, MMIOHandlerSystemControl.getInstance());
    	addHandler(0xBC200000, 0x8, new MMIOHandlerCpuBusFrequency(0xBC200000));
    	addHandler(MMIOHandlerInterruptMan.BASE_ADDRESS, 0x30, MMIOHandlerInterruptMan.getProxyInstance());
    	addHandler(0xBC500000, 0x10, new int[] { 0x0100 }, new MMIOHandlerTimer(0xBC500000));
    	addHandler(0xBC500010, 0x10, new int[] { 0x0100 }, new MMIOHandlerTimer(0xBC500010));
    	addHandler(0xBC500020, 0x10, new int[] { 0x0100 }, new MMIOHandlerTimer(0xBC500020));
    	addHandler(0xBC500030, 0x10, new int[] { 0x0100 }, new MMIOHandlerTimer(0xBC500030));
    	addHandler(0xBC600000, 0x14, new MMIOHandlerSystemTime(0xBC600000));
    	addHandler(0xBC800000, 0x1D4, new MMIOHandlerDmacplus(0xBC800000));
    	addHandler(0xBC900000, 0x1F4, new MMIOHandlerDmac(0xBC900000));
    	addHandler(0xBCA00000, 0x1F4, new MMIOHandlerDmac(0xBCA00000));
    	addHandlerRW(0xBCC00000, 0x74);
    	addHandlerRO(0xBCC00010, 0x4);
    	addHandler(MMIOHandlerDdr.BASE_ADDRESS, 0x48, MMIOHandlerDdr.getInstance());
    	addHandler(MMIOHandlerNand.BASE_ADDRESS, 0x304, MMIOHandlerNand.getInstance());
    	addHandler(0xBD200000, 0x44, new MMIOHandlerMemoryStick(0xBD200000));
    	addHandlerRW(0xBD300000, 0x44); // Wlan
    	addHandler(MMIOHandlerGe.BASE_ADDRESS, 0xE50, MMIOHandlerGe.getInstance());
    	addHandlerRW(0xBD500000, 0x94); // Graphics engine (ge)
    	addHandlerRO(0xBD500010, 0x4);
    	addHandler(0xBD600000, 0x50, new MMIOHandlerAta2(0xBD600000));
    	addHandler(MMIOHandlerAta.BASE_ADDRESS, 0xF, MMIOHandlerAta.getInstance());
    	addHandler(0xBDE00000, 0x3C, new MMIOHandlerKirk(0xBDE00000));
    	addHandler(0xBDF00000, 0x98, new MMIOHandlerUmd(0xBDF00000));
    	addHandler(0xBE000000, 0x80, new MMIOHandlerAudio(0xBE000000));
    	addHandlerRW(0xBE140000, 0x204); // LCDC / display
    	write32(0xBE140010, 0x29);
    	write32(0xBE140014, 0x02);
    	write32(0xBE140018, 0x02);
    	write32(0xBE14001C, Screen.width);
    	write32(0xBE140020, 0x0A);
    	write32(0xBE140024, 0x02);
    	write32(0xBE140028, 0x02);
    	write32(0xBE14002C, Screen.height);
    	write32(0xBE140048, Screen.width);
    	write32(0xBE14004C, Screen.height);
    	write32(0xBE140050, 0x01);
    	addHandler(0xBE200000, 0x30, new MMIOHandlerI2c(0xBE200000));
    	addHandler(MMIOHandlerGpio.BASE_ADDRESS, 0x4C, MMIOHandlerGpio.getInstance());
    	addHandlerRW(0xBE300000, 0x48); // Power management
    	addHandlerRW(0xBE4C0000, 0x48); // UART4 Uart4/kernel debug(?) UART (IPL, uart4, reboot)
    	addHandlerRW(0xBE500000, 0x3C); // UART3 headphone remote SIO (hpremote)
    	addHandler(MMIOHandlerSyscon.BASE_ADDRESS, 0x28, MMIOHandlerSyscon.getInstance());
    	addHandler(MMIOHandlerDisplayController.BASE_ADDRESS, 0x28, MMIOHandlerDisplayController.getInstance());
    	addHandlerRW(0xBFC00000, 0x1000);
    	write32(0xBFC00200, 0x2E547106);
    	write32(0xBFC00204, 0xFBDFC08B);
    	write32(0xBFC00208, 0x087FCC08);
    	write32(0xBFC0020C, 0xAA60334E);
    	addHandler(MMIOHandlerMeCore.BASE_ADDRESS, 0x2C, MMIOHandlerMeCore.getInstance());
    	addHandler(MMIOHandlerNandPage.BASE_ADDRESS1, 0x90C, MMIOHandlerNandPage.getInstance());
    	addHandler(MMIOHandlerNandPage.BASE_ADDRESS2, 0x90C, MMIOHandlerNandPage.getInstance());
    }

    protected void addHandler(int baseAddress, int length, IMMIOHandler handler) {
    	addHandler(baseAddress, length, null, handler);
    }

    private void addHandler(int baseAddress, int length, int[] additionalOffsets, IMMIOHandler handler) {
    	for (int i = 0; i < length; i++) {
    		handlers.put(baseAddress + i, handler);
    	}

    	if (additionalOffsets != null) {
	    	for (int offset : additionalOffsets) {
	    		handlers.put(baseAddress + offset, handler);
	    	}
    	}
    }

    protected void addHandlerRW(int baseAddress, int length) {
    	addHandlerRW(baseAddress, length, null);
    }

    protected void addHandlerRW(int baseAddress, int length, Logger log) {
    	MMIOHandlerReadWrite handler = new MMIOHandlerReadWrite(baseAddress, length);
    	if (log != null) {
    		handler.setLogger(log);
    	}
    	addHandler(baseAddress, length, handler);
    }

    protected void addHandlerRO(int baseAddress, int length) {
    	addHandler(baseAddress, length, new MMIOHandlerReadOnly(baseAddress, length));
    }

    private IMMIOHandler getHandler(int address) {
    	return handlers.get(address);
    }

    private boolean hasHandler(int address) {
    	return handlers.containsKey(address);
    }

    public static boolean isAddressGood(int address) {
        return validMemoryPage[address >>> MEMORY_PAGE_SHIFT];
    }

    @Override
    public int normalize(int address) {
    	if (hasHandler(address)) {
    		return address;
    	}
    	return mem.normalize(address);
    }

    public static int normalizeAddress(int addr) {
		// Transform address 0x1nnnnnnn into 0xBnnnnnnn
		if (addr >= (START_IO_0 & Memory.addressMask) && addr <= (END_IO_1 & Memory.addressMask)) {
			addr |= (START_IO_0 & ~Memory.addressMask);
		}

		return addr;
	}

    @Override
	public int read8(int address) {
    	IMMIOHandler handler = getHandler(address);
    	if (handler != null) {
    		return handler.read8(address);
    	}
		return mem.read8(address);
	}

	@Override
	public int read16(int address) {
    	IMMIOHandler handler = getHandler(address);
    	if (handler != null) {
    		return handler.read16(address);
    	}
		return mem.read16(address);
	}

	@Override
	public int read32(int address) {
    	IMMIOHandler handler = getHandler(address);
    	if (handler != null) {
    		return handler.read32(address);
    	}
		return mem.read32(address);
	}

	@Override
	public void write8(int address, byte data) {
    	IMMIOHandler handler = getHandler(address);
    	if (handler != null) {
    		handler.write8(address, data);
    	} else {
    		mem.write8(address, data);
    	}
	}

	@Override
	public void write16(int address, short data) {
    	IMMIOHandler handler = getHandler(address);
    	if (handler != null) {
    		handler.write16(address, data);
    	} else {
    		mem.write16(address, data);
    	}
	}

	@Override
	public void write32(int address, int data) {
    	IMMIOHandler handler = getHandler(address);
    	if (handler != null) {
    		handler.write32(address, data);
    	} else {
    		mem.write32(address, data);
    	}
	}

	@Override
	public void memset(int address, byte data, int length) {
		mem.memset(address, data, length);
	}

	@Override
	public Buffer getMainMemoryByteBuffer() {
		return mem.getMainMemoryByteBuffer();
	}

	@Override
	public Buffer getBuffer(int address, int length) {
		return mem.getBuffer(address, length);
	}

	@Override
	public void copyToMemory(int address, ByteBuffer source, int length) {
		mem.copyToMemory(address, source, length);
	}

	@Override
	protected void memcpy(int destination, int source, int length, boolean checkOverlap) {
		if (((destination | source | length) & 0x3) == 0 && !checkOverlap) {
			for (int i = 0; i < length; i += 4) {
				write32(destination + i, read32(source + i));
			}
		} else {
			if (checkOverlap) {
				mem.memmove(destination, source, length);
			} else {
				mem.memcpy(destination, source, length);
			}
		}
	}
}
