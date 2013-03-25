package shared.enums;

import transducer.TChannel;

/**
 * Enum to keep track of what type of machine the machine is.
 */
public enum MachineType {

	// Offline Machines
	CROSS_SEAMER {
		public String toString() {
			return "Cross Seamer";
		}
	},
	DRILL {
		public String toString() {
			return "Drill";
		}
	},
	GRINDER {
		public String toString() {
			return "Grinder";
		}
	},
	MANUAL_BREAKOUT {
		public String toString() {
			return "Manual Breakout";
		}
	},

	// Online Machines
	CUTTER {
		public String toString() {
			return "Cutter";
		}
	},
	WASHER {
		public String toString() {
			return "Washer";
		}
	},
	UV_LAMP {
		public String toString() {
			return "UV Lamp";
		}
	},
	OVEN {
		public String toString() {
			return "Oven";
		}
	},
	PAINT {
		public String toString() {
			return "Paint";
		}
	},
	BREAKOUT {
		public String toString() {
			return "Breakout";
		}
	};

	public TChannel getChannel() {
		switch (this) {
		// Offline Machines
		case CROSS_SEAMER:
			return TChannel.CROSS_SEAMER;
		case DRILL:
			return TChannel.DRILL;
		case GRINDER:
			return TChannel.GRINDER;
		case MANUAL_BREAKOUT:
			return TChannel.MANUAL_BREAKOUT;
		// Online Machines
		case CUTTER:
			return TChannel.CUTTER;
		case WASHER:
			return TChannel.WASHER;
		case UV_LAMP:
			return TChannel.UV_LAMP;
		case OVEN:
			return TChannel.OVEN;
		case PAINT:
			return TChannel.PAINTER;
		case BREAKOUT:
			return TChannel.BREAKOUT;
		default:
			return TChannel.NULL_CHANNEL;
		}
	}
	
	// Quick test
	public static void main(String[] args) {
		MachineType type = MachineType.CROSS_SEAMER;
		TChannel ch = type.getChannel();
		System.out.println("Type is "+type+", channel is "+ch);
		
		type = MachineType.PAINT;
		ch = type.getChannel();
		System.out.println("Type is "+type+", channel is "+ch);
	}

}

