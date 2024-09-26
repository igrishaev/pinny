package deed;

public record Options(
        long derefTimeoutMs,
        int objectChunkSize,
        int byteChunkSize,
        int bufInputSize,
        int bufOutputSize,
        int uncountableMaxItems,
        boolean encodeUnsupported,
        boolean ioUseTempFile,
        boolean saveMeta,
        boolean append,
        String cipherAlgorithm,
        byte[] cipherSecret
) {

    public static Options standard() {
        return new Builder().build();
    }

    @SuppressWarnings("unused")
    public static Builder builder() {
        return new Builder();
    }

    public final static class Builder {

        private long derefTimeoutMs = Const.OPT_DEREF_TIMEOUT_MS;
        private int objectChunkSize = Const.OPT_OBJECT_CHUNK_SIZE;
        private int byteChunkSize = Const.OPT_BYTE_CHUNK_SIZE;
        private int bufInputSize = Const.OPT_IN_BUF_SIZE;
        private int bufOutputSize = Const.OPT_OUT_BUF_SIZE;
        private int uncountableMaxItems = Const.OPT_UNCOUNTABLE_MAX_ITEMS;
        private boolean encodeUnsupported = Const.OPT_ENCODE_UNSUPPORTED;
        private boolean ioUseTempFile = Const.OPT_USE_IO_TEMP_FILE;
        private boolean saveMeta = Const.OPT_SAVE_META;
        private boolean append = Const.OPT_APPEND;
        private String cipherAlgorithm = null;
        private byte[] cipherSecret = null;

        @SuppressWarnings("unused")
        public Builder derefTimeoutMs(final long derefTimeoutMs) {
            this.derefTimeoutMs = derefTimeoutMs;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder objectChunkSize(final int objectChunkSize) {
            this.objectChunkSize = objectChunkSize;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder byteChunkSize(final int byteChunkSize) {
            this.byteChunkSize = byteChunkSize;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder bufInputSize(final int bufInputSize) {
            this.bufInputSize = bufInputSize;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder bufOutputSize(final int bufOutputSize) {
            this.bufOutputSize = bufOutputSize;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder uncountableMaxItems(final int uncountableMaxItems) {
            this.uncountableMaxItems = uncountableMaxItems;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder encodeUnsupported(final boolean encodeUnsupported) {
            this.encodeUnsupported = encodeUnsupported;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder ioUseTempFile(final boolean ioUseTempFile) {
            this.ioUseTempFile = ioUseTempFile;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder saveMeta(final boolean saveMeta) {
            this.saveMeta = saveMeta;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder append(final boolean append) {
            this.append = append;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder cipherAlgorithm(final String cipherAlgorithm) {
            this.cipherAlgorithm = cipherAlgorithm;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder cipherSecret(final byte[] cipherSecret) {
            this.cipherSecret = cipherSecret;
            return this;
        }

        public Options build() {
            return new Options(
                    derefTimeoutMs,
                    objectChunkSize,
                    byteChunkSize,
                    bufInputSize,
                    bufOutputSize,
                    uncountableMaxItems,
                    encodeUnsupported,
                    ioUseTempFile,
                    saveMeta,
                    append,
                    cipherAlgorithm,
                    cipherSecret
            );
        }
    }


}
