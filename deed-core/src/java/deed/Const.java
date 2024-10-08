package deed;

public class Const {
    public static short HEADER_GAP = 30;
    public static short HEADER_VERSION = 1;

    public static long OPT_DEREF_TIMEOUT_MS = 5000;
    public static int OPT_OBJECT_CHUNK_SIZE = 0xFF;
    public static int OPT_BYTE_CHUNK_SIZE = 0xFFFF;
    public static boolean OPT_USE_IO_TEMP_FILE = false;
    public static boolean OPT_APPEND = false;

    public static int OPT_UNCOUNTABLE_MAX_ITEMS = Integer.MAX_VALUE;
    public static boolean OPT_ENCODE_UNSUPPORTED = true;
    public static boolean OPT_SAVE_META = true;

    public static byte BYTE_MINUS_ONE = -1;
    public static byte BYTE_ZERO = 0;
    public static byte BYTE_ONE = 1;
    public static double DOUBLE_ONE = 1;
    public static double DOUBLE_MINUS_ONE = -1;
    public static double DOUBLE_ZERO = 0;
}
