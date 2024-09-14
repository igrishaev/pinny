package pinny;

import clojure.lang.*;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URI;
import java.time.*;
import java.util.*;
import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public final class Decoder implements Iterable<Object>, AutoCloseable {

    final private DataInputStream dataInputStream;
    final private BufferedInputStream bufferedInputStream;
    final private MultiFn mmDecode;
    final private EOF EOF;

    public Decoder(final MultiFn mmDecode, final InputStream inputStream) {
        this(mmDecode, inputStream, Options.standard());
    }

    private void readHeader() {
        readShort();
    }

    public Decoder(final MultiFn mmDecode, final InputStream inputStream, final Options options) {
        EOF = new EOF();
        this.mmDecode = mmDecode;
        InputStream source = inputStream;
        if (options.useGzip()) {
            try {
                source = new GZIPInputStream(inputStream);
            } catch (IOException e) {
                throw Err.error(e, "could not open a Gzip input stream");
            }
        }
        bufferedInputStream = new BufferedInputStream(source, Const.IN_BUF_SIZE);
        dataInputStream = new DataInputStream(bufferedInputStream);
        readHeader();
    }

    public short readShort() {
        try {
            return dataInputStream.readShort();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public long readLong() {
        try {
            return dataInputStream.readLong();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int readInteger() {
        try {
            return dataInputStream.readInt();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean readBoolean() {
        try {
            return dataInputStream.readBoolean();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public double readDouble() {
        try {
            return dataInputStream.readDouble();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    public float readFloat() {
        try {
            return dataInputStream.readFloat();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Atom readAtom() {
        final Object content = decode();
        return new Atom(content);
    }

    public Ref readRef() {
        final Object content = decode();
        return new Ref(content);
    }

    public String readString() {
        final int len = readInteger();
        final byte[] buf = new byte[len];
        try {
            dataInputStream.readFully(buf);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new String(buf, StandardCharsets.UTF_8);
    }

    public Keyword readKeyword() {
        final String payload = readString();
        return Keyword.intern(payload);
    }

    public Symbol readSymbol() {
        final String payload = readString();
        return Symbol.intern(payload);
    }

    public byte[] readBytes() {
        final int size = readInteger();
        final byte[] buf = new byte[size];
        try {
            dataInputStream.readFully(buf);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return buf;
    }

    public byte readByte() {
        try {
            return dataInputStream.readByte();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public BigInteger readBigInteger() {
        final byte[] buf = readBytes();
        return new BigInteger(buf);
    }

    public BigDecimal readBigDecimal() {
        final int scale = readInteger();
        final BigInteger unscaled = readBigInteger();
        return new BigDecimal(unscaled, scale);
    }

    public BigInt readBigInt() {
        final BigInteger bi = readBigInteger();
        return BigInt.fromBigInteger(bi);
    }

    public Ratio readRatio() {
        final BigInteger numerator = readBigInteger();
        final BigInteger denominator = readBigInteger();
        return new Ratio(numerator, denominator);
    }

    public IPersistentMap readClojureMap() {
        Object key;
        Object val;
        final int len = readInteger();
        ITransientMap m = PersistentArrayMap.EMPTY.asTransient();
        for (int i = 0; i < len; i++) {
            key = decode();
            val = decode();
            m = m.assoc(key, val);
        }
        return m.persistent();
    }

    public IPersistentCollection readClojureSet() {
        Object x;
        final int len = readInteger();
        ITransientCollection s = PersistentHashSet.EMPTY.asTransient();
        for (int i = 0; i < len; i++) {
            x = decode();
            s = s.conj(x);
        }
        return s.persistent();
    }

    public IPersistentCollection readClojureSortedSet() {
        Object x;
        IPersistentCollection s = PersistentTreeSet.EMPTY;
        final int len = readInteger();
        for (int i = 0; i < len; i++) {
            x = decode();
            s.cons(x);
            s = s.cons(x);
        }
        return s;
    }

    public PersistentTreeMap readClojureSortedMap() {
        Object key;
        Object val;
        PersistentTreeMap m = PersistentTreeMap.EMPTY;
        final int len = readInteger();
        for (int i = 0; i < len; i++) {
            key = decode();
            val = decode();
            m = m.assoc(key, val);
        }
        return m;
    }

    public ISeq readClojureSeq() {
        int limit;
        Object x;
        ITransientCollection v = PersistentVector.EMPTY.asTransient();
        while (true) {
            limit = readInteger();
            if (limit == 0) {
                break;
            } else {
                for (int i = 0; i < limit; i++) {
                    x = decode();
                    v = v.conj(x);
                }
            }
        }
        return v.persistent().seq();
    }

    public ArrayList<?> readChunkedList() {
        int limit;
        Object x;
        final ArrayList<Object> list = new ArrayList<>();
        while (true) {
            limit = readInteger();
            if (limit == 0) {
                break;
            } else {
                for (int i = 0; i < limit; i++) {
                    x = decode();
                    list.add(x);
                }
            }
        }
        return list;
    }

    public List<?> readJavaList() {
        Object x;
        final int len = readInteger();
        final ArrayList<Object> list = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            x = decode();
            list.add(x);
        }
        return list;
    }

    public Vector<?> readJavaVector() {
        Object x;
        final int len = readInteger();
        final Vector<Object> vector = new Vector<>(len);
        for (int i = 0; i < len; i++) {
            x = decode();
            vector.add(x);
        }
        return vector;
    }

    public IPersistentCollection readClojureVector() {
        Object x;
        final int len = readInteger();
        ITransientCollection v = PersistentVector.EMPTY.asTransient();
        for (int i = 0; i < len; i++) {
            x = decode();
            v = v.conj(x);
        }
        return v.persistent();
    }

    public PersistentQueue readClojureQueue() {
        Object x;
        final int len = readInteger();
        PersistentQueue q = PersistentQueue.EMPTY;
        for (int i = 0; i < len; i++) {
            x = decode();
            q = q.cons(x);
        }
        return q;
    }

    public Map<?,?> readJavaMap() {
        Object key;
        Object val;
        final int len = readInteger();
        HashMap<Object, Object> m = new HashMap<>(len);
        for (int i = 0; i < len; i++) {
            key = decode();
            val = decode();
            m.put(key, val);
        }
        return m;
    }

    public Pattern readRegex() {
        final String payload = readString();
        return Pattern.compile(payload);
    }

    public URL readURL() {
        final String payload = readString();
        try {
            //noinspection deprecation
            return new URL(payload);
        } catch (MalformedURLException e) {
            throw Err.error(e, "couldn't parse URL: %s", payload);
        }
    }

    public URI readURI() {
        final String payload = readString();
        return URI.create(payload);
    }

    public char readCharacter() {
        try {
            return dataInputStream.readChar();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public UUID readUUID() {
        final long hi = readLong();
        final long lo = readLong();
        return new UUID(hi, lo);
    }

    public Object[] readObjectArray() {
        final int len = readInteger();
        final Object[] array = new Object[len];
        for (int i = 0; i < len; i++) {
            array[i] = decode();
        }
        return array;
    }

    public int[] readIntArray() {
        final int len = readInteger();
        final int[] array = new int[len];
        for (int i = 0; i < len; i++) {
            array[i] = readInteger();
        }
        return array;
    }

    public short[] readShortArray() {
        final int len = readInteger();
        final short[] array = new short[len];
        for (int i = 0; i < len; i++) {
            array[i] = readShort();
        }
        return array;
    }

    public boolean[] readBoolArray() {
        final int len = readInteger();
        final boolean[] array = new boolean[len];
        for (int i = 0; i < len; i++) {
            array[i] = readBoolean();
        }
        return array;
    }

    public float[] readFloatArray() {
        final int len = readInteger();
        final float[] array = new float[len];
        for (int i = 0; i < len; i++) {
            array[i] = readFloat();
        }
        return array;
    }

    public double[] readDoubleArray() {
        final int len = readInteger();
        final double[] array = new double[len];
        for (int i = 0; i < len; i++) {
            array[i] = readDouble();
        }
        return array;
    }

    public long[] readLongArray() {
        final int len = readInteger();
        final long[] array = new long[len];
        for (int i = 0; i < len; i++) {
            array[i] = readLong();
        }
        return array;
    }

    public char[] readCharArray() {
        final int len = readInteger();
        final char[] array = new char[len];
        for (int i = 0; i < len; i++) {
            array[i] = readCharacter();
        }
        return array;
    }

    public Future<?> readFuture() {
        final Object payload = decode();
        return CompletableFuture.completedFuture(payload);
    }

    public Date readUtilDate(){
        final long time = readLong();
        return new Date(time);
    }

    public java.sql.Date readSqlDate() {
        final long time = readLong();
        return new java.sql.Date(time);
    }

    public java.sql.Time readSqlTime() {
        final long time = readLong();
        return new java.sql.Time(time);
    }

    public java.sql.Timestamp readSqlTimestamp() {
        final long time = readLong();
        return new java.sql.Timestamp(time);
    }

    public Instant readInstant() {
        final long secs = readLong();
        final int nanos = readInteger();
        return Instant.ofEpochSecond(secs, nanos);
    }

    public Duration readDuration() {
        final long seconds = readLong();
        final int nanos = readInteger();
        return Duration.ofSeconds(seconds, nanos);
    }

    public Period readPeriod() {
        final int years = readInteger();
        final int months = readInteger();
        final int days = readInteger();
        return Period.of(years, months, days);
    }

    public LocalDateTime readLocalDateTime() {
        final long seconds = readLong();
        final int nanos = readInteger();
        return LocalDateTime.ofEpochSecond(seconds, nanos, ZoneOffset.UTC);
    }

    public OffsetDateTime readOffsetDateTime() {
        final long epoch = readLong();
        final int nanos = readInteger();
        final int offset = readInteger();
        final LocalDateTime localDateTime = LocalDateTime.ofEpochSecond(epoch, nanos, ZoneOffset.UTC);
        final ZoneOffset zoneOffset = ZoneOffset.ofTotalSeconds(offset);
        return OffsetDateTime.of(localDateTime, zoneOffset);
    }

    public ZonedDateTime readZonedDateTime() {
        final long epoch = readLong();
        final int nanos = readInteger();
        final String zoneName = readString();
        final LocalDateTime localDateTime = LocalDateTime.ofEpochSecond(epoch, nanos, ZoneOffset.UTC);
        final ZoneId zoneId = ZoneId.of(zoneName);
        return ZonedDateTime.of(localDateTime, zoneId);
    }

    public OffsetTime readOffsetTime() {
        final long nanos = readLong();
        final int offset = readInteger();
        final LocalTime localTime = LocalTime.ofNanoOfDay(nanos);
        final ZoneOffset zoneOffset = ZoneOffset.ofTotalSeconds(offset);
        return OffsetTime.of(localTime, zoneOffset);
    }

    public ZoneId readZoneId() {
        final String id = readString();
        return ZoneId.of(id);
    }

    public LocalTime readLocalTime() {
        final long nanos = readLong();
        return LocalTime.ofNanoOfDay(nanos);
    }

    public LocalDate readLocalDate() {
        final long days = readLong();
        return LocalDate.ofEpochDay(days);
    }

    public MapEntry readClojureMapEntry() {
        final Object key = decode();
        final Object val = decode();
        return MapEntry.create(key, val);
    }

    public Map.Entry<?,?> readJavaMapEntry() {
        final Object key = decode();
        final Object val = decode();
        return new MEntry(key, val);
    }

    public StackTraceElement readStackTraceElement() {
        final String className = readString();
        final String methodName = readString();
        final String fileName = readString();
        final int lineNumber = readInteger();
        return new StackTraceElement(className, methodName, fileName, lineNumber);
    }

    public Throwable tryDecodeThrowable() {
        final Object x = decode();
        if (x instanceof Throwable t) {
            return t;
        } else {
            throw Err.error("unexpected non-Throwable object: %s %s", x.getClass(), x);
        }
    }

    public Throwable readThrowable() {
        final boolean hasMessage = readBoolean();
        String message = null;
        if (hasMessage) {
            message = readString();
        }
        final int traceLen = readInteger();
        final StackTraceElement[] trace = new StackTraceElement[traceLen];
        for (int i = 0; i < traceLen; i++) {
            trace[i] = readStackTraceElement();
        }
        final boolean hasCause = readBoolean();
        Throwable cause = null;
        if (hasCause) {
            cause = tryDecodeThrowable();
        }
        final int suppressedLen = readInteger();
        final Throwable[] suppressed = new Throwable[suppressedLen];
        for (int i = 0; i < suppressedLen; i++) {
            suppressed[i] = tryDecodeThrowable();
        }

        final Throwable result = new Throwable(message, cause);
        result.setStackTrace(trace);
        for (Throwable s: suppressed) {
            result.addSuppressed(s);
        }
        return result;
    }

    public ExceptionInfo readExceptionInfo() {
        final IPersistentMap data = readClojureMap();
        final Throwable t = readThrowable();

        final ExceptionInfo result = new ExceptionInfo(
                t.getMessage(),
                data,
                t.getCause()
        );
        result.setStackTrace(t.getStackTrace());
        for (Throwable s: t.getSuppressed()) {
            result.addSuppressed(s);
        }
        return result;
    }

    public Object decode() {
        int r;

        bufferedInputStream.mark(1);
        try {
            r = bufferedInputStream.read();
            bufferedInputStream.reset();
        } catch (IOException e) {
            throw Err.error(e, "could not read() from the input stream");
        }

        if (r == -1) {
            return EOF;
        }

        final short oid = readShort();

        return switch (oid) {
            case OID.EX_INFO -> readExceptionInfo();
            case OID.THROWABLE -> readThrowable();
            case OID.JVM_STREAM -> readChunkedList().stream();
            case OID.JVM_ITERATOR -> readChunkedList().iterator();
            case OID.JVM_ITERABLE -> readChunkedList();
            case OID.JVM_LIST -> readJavaList();
            case OID.JVM_VECTOR -> readJavaVector();
            case OID.JVM_VECTOR_EMPTY -> new Vector<>();
            case OID.JVM_LIST_EMPTY -> List.of();
            case OID.JVM_MAP_ENTRY -> readJavaMapEntry();
            case OID.CLJ_MAP_ENTRY -> readClojureMapEntry();
            case OID.CLJ_QUEUE_EMPTY -> PersistentQueue.EMPTY;
            case OID.CLJ_QUEUE -> readClojureQueue();
            case OID.CLJ_LIST_EMPTY -> PersistentList.EMPTY;
            case OID.CLJ_LIST, OID.CLJ_VEC -> readClojureVector();
            case OID.CLJ_SEQ, OID.CLJ_LAZY_SEQ -> readClojureSeq();
            case OID.SQL_TIME -> readSqlTime();
            case OID.SQL_DATE -> readSqlDate();
            case OID.SQL_TIMESTAMP -> readSqlTimestamp();
            case OID.DT_DURATION -> readDuration();
            case OID.DT_PERIOD -> readPeriod();
            case OID.DT_ZONE_ID -> readZoneId();
            case OID.DT_OFFSET_TIME -> readOffsetTime();
            case OID.DT_OFFSET_DATETIME -> readOffsetDateTime();
            case OID.DT_ZONED_DATETIME -> readZonedDateTime();
            case OID.DT_LOCAL_DATETIME -> readLocalDateTime();
            case OID.CLJ_RECORD, OID.CLJ_MAP -> readClojureMap();
            case OID.CLJ_SET_EMPTY -> PersistentHashSet.EMPTY;
            case OID.CLJ_SET -> readClojureSet();
            case OID.CLJ_SORTED_SET_EMPTY -> PersistentTreeSet.EMPTY;
            case OID.CLJ_SORTED_SET -> readClojureSortedSet();
            case OID.CLJ_SORTED_MAP_EMPTY -> PersistentTreeMap.EMPTY;
            case OID.CLJ_SORTED_MAP -> readClojureSortedMap();
            case OID.DT_LOCAL_DATE -> readLocalDate();
            case OID.DT_LOCAL_TIME -> readLocalTime();
            case OID.UTIL_DATE -> readUtilDate();
            case OID.DT_INSTANT -> readInstant();
            case OID.CLJ_VEC_EMPTY -> PersistentVector.EMPTY;
            case OID.DOUBLE -> readDouble();
            case OID.DOUBLE_ONE -> Const.DOUBLE_ONE;
            case OID.DOUBLE_MINUS_ONE -> Const.DOUBLE_MINUS_ONE;
            case OID.DOUBLE_ZERO -> Const.DOUBLE_ZERO;
            case OID.REGEX -> readRegex();
            case OID.NULL -> null;
            case OID.BOOL_FALSE -> false;
            case OID.BOOL_TRUE -> true;
            case OID.SHORT_ONE -> (short)1;
            case OID.SHORT_MINUS_ONE -> (short)-1;
            case OID.SHORT_ZERO -> (short)0;
            case OID.SHORT -> readShort();
            case OID.INT -> readInteger();
            case OID.INT_ONE -> 1;
            case OID.INT_ZERO -> 0;
            case OID.INT_MINUS_ONE -> -1;
            case OID.LONG -> readLong();
            case OID.LONG_ONE -> (long)1;
            case OID.LONG_MINUS_ONE -> (long)-1;
            case OID.LONG_ZERO -> (long)0;
            case OID.FLOAT -> readFloat();
            case OID.FLOAT_ONE -> (float)1;
            case OID.FLOAT_MINUS_ONE -> (float)-1;
            case OID.FLOAT_ZERO -> (float)0;
            case OID.CLJ_ATOM -> readAtom();
            case OID.CLJ_REF -> readRef();
            case OID.STRING -> readString();
            case OID.STRING_EMPTY -> "";
            case OID.CHAR -> readCharacter();
            case OID.URL -> readURL();
            case OID.URI -> readURI();
            case OID.UUID -> readUUID();
            case OID.CLJ_KEYWORD -> readKeyword();
            case OID.CLJ_SYMBOL -> readSymbol();
            case OID.CLJ_RATIO -> readRatio();
            case OID.JVM_BIG_INT -> readBigInteger();
            case OID.JVM_BIG_DEC -> readBigDecimal();
            case OID.CLJ_BIG_INT -> readBigInt();
            case OID.CLJ_MAP_EMPTY -> PersistentArrayMap.EMPTY;
            case OID.JVM_MAP -> readJavaMap();
            case OID.BYTE -> readByte();
            case OID.BYTE_MINUS_ONE -> Const.BYTE_MINUS_ONE;
            case OID.BYTE_ZERO -> Const.BYTE_ZERO;
            case OID.BYTE_ONE -> Const.BYTE_ONE;
            case OID.ARR_BYTE -> readBytes();
            case OID.ARR_OBJ -> readObjectArray();
            case OID.ARR_INT -> readIntArray();
            case OID.ARR_SHORT -> readShortArray();
            case OID.ARR_BOOL -> readBoolArray();
            case OID.ARR_FLOAT -> readFloatArray();
            case OID.ARR_DOUBLE -> readDoubleArray();
            case OID.ARR_LONG -> readLongArray();
            case OID.ARR_CHAR -> readCharArray();
            case OID.FUTURE -> readFuture();
            default -> mmDecode.invoke(oid, this);
        };
    }

    @SuppressWarnings("unused")
    public ISeq decodeSeq() {
        return RT.chunkIteratorSeq(this.iterator());
    }

    @Override
    public Iterator<Object> iterator() {
        return new Iterator<>() {

            private Object next;

            @Override
            public boolean hasNext() {
                next = decode();
                return next != EOF;
            }

            @Override
            public Object next() {
                return next;
            }
        };
    }

    @Override
    public void close() {
        try {
            dataInputStream.close();
        } catch (IOException e) {
            throw Err.error(e, "could not close the stream");
        }
    }
}
