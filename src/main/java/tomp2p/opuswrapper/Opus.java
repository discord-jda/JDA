package tomp2p.opuswrapper;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.ptr.FloatByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.ptr.ShortByReference;
import net.dv8tion.jda.core.managers.impl.AudioManagerImpl;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * This is taken from <a href="https://github.com/tbocek/opus-wrapper">https://github.com/tbocek/opus-wrapper</a><br>
 * Slight modification to how it loads the native library.<br>
 * Credit for creation of this wrapper goes to the original developer:
 * <a href="https://github.com/tbocek">Thomas Bocek (tbocek)</a>
 * <p>
 * Completely removed all of the javadoc in this class because it was terrible and throwing errors when gradle
 * attempted to create javadoc for it. If you want to see the javadoc, please refer to the the original developer's
 * Github repo.
 */
public interface Opus extends Library {
    public static final Opus INSTANCE = (Opus)Native.loadLibrary(AudioManagerImpl.OPUS_LIB_NAME, Opus.class);

    //  ****** Constants

    public static final int OPUS_GET_LSB_DEPTH_REQUEST = (int)4037;

    public static final int OPUS_GET_APPLICATION_REQUEST = (int)4001;

    public static final int OPUS_GET_FORCE_CHANNELS_REQUEST = (int)4023;

    public static final int OPUS_GET_VBR_REQUEST = (int)4007;

    public static final int OPUS_GET_BANDWIDTH_REQUEST = (int)4009;

    public static final int OPUS_SET_BITRATE_REQUEST = (int)4002;

    public static final int OPUS_SET_BANDWIDTH_REQUEST = (int)4008;

    public static final int OPUS_SIGNAL_MUSIC = (int)3002;

    public static final int OPUS_RESET_STATE = (int)4028;

    public static final int OPUS_FRAMESIZE_2_5_MS = (int)5001;

    public static final int OPUS_GET_COMPLEXITY_REQUEST = (int)4011;

    public static final int OPUS_FRAMESIZE_40_MS = (int)5005;

    public static final int OPUS_SET_PACKET_LOSS_PERC_REQUEST = (int)4014;

    public static final int OPUS_GET_VBR_CONSTRAINT_REQUEST = (int)4021;

    public static final int OPUS_SET_INBAND_FEC_REQUEST = (int)4012;

    public static final int OPUS_APPLICATION_RESTRICTED_LOWDELAY = (int)2051;

    public static final int OPUS_BANDWIDTH_FULLBAND = (int)1105;

    public static final int OPUS_SET_VBR_REQUEST = (int)4006;

    public static final int OPUS_BANDWIDTH_SUPERWIDEBAND = (int)1104;

    public static final int OPUS_SET_FORCE_CHANNELS_REQUEST = (int)4022;

    public static final int OPUS_APPLICATION_VOIP = (int)2048;

    public static final int OPUS_SIGNAL_VOICE = (int)3001;

    public static final int OPUS_GET_FINAL_RANGE_REQUEST = (int)4031;

    public static final int OPUS_BUFFER_TOO_SMALL = (int)-2;

    public static final int OPUS_SET_COMPLEXITY_REQUEST = (int)4010;

    public static final int OPUS_FRAMESIZE_ARG = (int)5000;

    public static final int OPUS_GET_LOOKAHEAD_REQUEST = (int)4027;

    public static final int OPUS_GET_INBAND_FEC_REQUEST = (int)4013;

    public static final int OPUS_BITRATE_MAX = (int)-1;

    public static final int OPUS_FRAMESIZE_5_MS = (int)5002;

    public static final int OPUS_BAD_ARG = (int)-1;

    public static final int OPUS_GET_PITCH_REQUEST = (int)4033;

    public static final int OPUS_SET_SIGNAL_REQUEST = (int)4024;

    public static final int OPUS_FRAMESIZE_20_MS = (int)5004;

    public static final int OPUS_APPLICATION_AUDIO = (int)2049;

    public static final int OPUS_GET_DTX_REQUEST = (int)4017;

    public static final int OPUS_FRAMESIZE_10_MS = (int)5003;

    public static final int OPUS_SET_LSB_DEPTH_REQUEST = (int)4036;

    public static final int OPUS_UNIMPLEMENTED = (int)-5;

    public static final int OPUS_GET_PACKET_LOSS_PERC_REQUEST = (int)4015;

    public static final int OPUS_INVALID_STATE = (int)-6;

    public static final int OPUS_SET_EXPERT_FRAME_DURATION_REQUEST = (int)4040;

    public static final int OPUS_FRAMESIZE_60_MS = (int)5006;

    public static final int OPUS_GET_BITRATE_REQUEST = (int)4003;

    public static final int OPUS_INTERNAL_ERROR = (int)-3;

    public static final int OPUS_SET_MAX_BANDWIDTH_REQUEST = (int)4004;

    public static final int OPUS_SET_VBR_CONSTRAINT_REQUEST = (int)4020;

    public static final int OPUS_GET_MAX_BANDWIDTH_REQUEST = (int)4005;

    public static final int OPUS_BANDWIDTH_NARROWBAND = (int)1101;

    public static final int OPUS_SET_GAIN_REQUEST = (int)4034;

    public static final int OPUS_SET_PREDICTION_DISABLED_REQUEST = (int)4042;

    public static final int OPUS_SET_APPLICATION_REQUEST = (int)4000;

    public static final int OPUS_SET_DTX_REQUEST = (int)4016;

    public static final int OPUS_BANDWIDTH_MEDIUMBAND = (int)1102;

    public static final int OPUS_GET_SAMPLE_RATE_REQUEST = (int)4029;

    public static final int OPUS_GET_EXPERT_FRAME_DURATION_REQUEST = (int)4041;

    public static final int OPUS_AUTO = (int)-1000;

    public static final int OPUS_GET_SIGNAL_REQUEST = (int)4025;

    public static final int OPUS_GET_LAST_PACKET_DURATION_REQUEST = (int)4039;

    public static final int OPUS_GET_PREDICTION_DISABLED_REQUEST = (int)4043;

    public static final int OPUS_GET_GAIN_REQUEST = (int)4045;

    public static final int OPUS_BANDWIDTH_WIDEBAND = (int)1103;

    public static final int OPUS_INVALID_PACKET = (int)-4;

    public static final int OPUS_ALLOC_FAIL = (int)-7;

    public static final int OPUS_OK = (int)0;


    public static final int OPUS_MULTISTREAM_GET_DECODER_STATE_REQUEST = (int)5122;

    public static final int OPUS_MULTISTREAM_GET_ENCODER_STATE_REQUEST = (int)5120;


    int opus_encoder_get_size(int channels);

    PointerByReference opus_encoder_create(int Fs, int channels, int application, IntBuffer error);

    int opus_encoder_init(PointerByReference st, int Fs, int channels, int application);

    int opus_encode(PointerByReference st, ShortBuffer pcm, int frame_size, ByteBuffer data, int max_data_bytes);

    int opus_encode(PointerByReference st, ShortByReference pcm, int frame_size, Pointer data, int max_data_bytes);

    int opus_encode_float(PointerByReference st, float pcm[], int frame_size, ByteBuffer data, int max_data_bytes);

    int opus_encode_float(PointerByReference st, FloatByReference pcm, int frame_size, Pointer data, int max_data_bytes);

    void opus_encoder_destroy(PointerByReference st);

    int opus_encoder_ctl(PointerByReference st, int request, Object... varargs);

    int opus_decoder_get_size(int channels);

    PointerByReference opus_decoder_create(int Fs, int channels, IntBuffer error);

    int opus_decoder_init(PointerByReference st, int Fs, int channels);

    int opus_decode(PointerByReference st, byte data[], int len, ShortBuffer pcm, int frame_size, int decode_fec);

    int opus_decode(PointerByReference st, Pointer data, int len, ShortByReference pcm, int frame_size, int decode_fec);

    int opus_decode_float(PointerByReference st, byte data[], int len, FloatBuffer pcm, int frame_size, int decode_fec);

    int opus_decode_float(PointerByReference st, Pointer data, int len, FloatByReference pcm, int frame_size, int decode_fec);

    int opus_decoder_ctl(PointerByReference st, int request, Object... varargs);

    void opus_decoder_destroy(PointerByReference st);

    int opus_packet_parse(byte data[], int len, ByteBuffer out_toc, byte frames[], ShortBuffer size, IntBuffer payload_offset);

    int opus_packet_get_bandwidth(byte data[]);

    int opus_packet_get_samples_per_frame(byte data[], int Fs);

    int opus_packet_get_nb_channels(byte data[]);

    int opus_packet_get_nb_frames(byte packet[], int len);

    int opus_packet_get_nb_samples(byte packet[], int len, int Fs);

    int opus_decoder_get_nb_samples(PointerByReference dec, byte packet[], int len);

    int opus_decoder_get_nb_samples(PointerByReference dec, Pointer packet, int len);

    void opus_pcm_soft_clip(FloatBuffer pcm, int frame_size, int channels, FloatBuffer softclip_mem);

    int opus_repacketizer_get_size();

    PointerByReference opus_repacketizer_init(PointerByReference rp);

    PointerByReference opus_repacketizer_create();

    void opus_repacketizer_destroy(PointerByReference rp);

    int opus_repacketizer_cat(PointerByReference rp, byte data[], int len);

    int opus_repacketizer_cat(PointerByReference rp, Pointer data, int len);

    int opus_repacketizer_out_range(PointerByReference rp, int begin, int end, ByteBuffer data, int maxlen);

    int opus_repacketizer_out_range(PointerByReference rp, int begin, int end, Pointer data, int maxlen);

    int opus_repacketizer_get_nb_frames(PointerByReference rp);

    int opus_repacketizer_out(PointerByReference rp, ByteBuffer data, int maxlen);

    int opus_repacketizer_out(PointerByReference rp, Pointer data, int maxlen);

    int opus_packet_pad(ByteBuffer data, int len, int new_len);

    int opus_packet_unpad(ByteBuffer data, int len);

    int opus_multistream_packet_pad(ByteBuffer data, int len, int new_len, int nb_streams);

    int opus_multistream_packet_unpad(ByteBuffer data, int len, int nb_streams);
    public static class OpusDecoder extends PointerType {
        public OpusDecoder(Pointer address) {
            super(address);
        }
        public OpusDecoder() {
            super();
        }
    };
    public static class OpusEncoder extends PointerType {
        public OpusEncoder(Pointer address) {
            super(address);
        }
        public OpusEncoder() {
            super();
        }
    };
    public static class OpusRepacketizer extends PointerType {
        public OpusRepacketizer(Pointer address) {
            super(address);
        }
        public OpusRepacketizer() {
            super();
        }
    };


    String opus_strerror(int error);

    String opus_get_version_string();

    int opus_multistream_encoder_get_size(int streams, int coupled_streams);

    int opus_multistream_surround_encoder_get_size(int channels, int mapping_family);

    PointerByReference opus_multistream_encoder_create(int Fs, int channels, int streams, int coupled_streams, byte mapping[], int application, IntBuffer error);

    PointerByReference opus_multistream_surround_encoder_create(int Fs, int channels, int mapping_family, IntBuffer streams, IntBuffer coupled_streams, ByteBuffer mapping, int application, IntBuffer error);

    int opus_multistream_encoder_init(PointerByReference st, int Fs, int channels, int streams, int coupled_streams, byte mapping[], int application);

    int opus_multistream_encoder_init(PointerByReference st, int Fs, int channels, int streams, int coupled_streams, Pointer mapping, int application);

    int opus_multistream_surround_encoder_init(PointerByReference st, int Fs, int channels, int mapping_family, IntBuffer streams, IntBuffer coupled_streams, ByteBuffer mapping, int application);

    int opus_multistream_surround_encoder_init(PointerByReference st, int Fs, int channels, int mapping_family, IntByReference streams, IntByReference coupled_streams, Pointer mapping, int application);

    int opus_multistream_encode(PointerByReference st, ShortBuffer pcm, int frame_size, ByteBuffer data, int max_data_bytes);

    int opus_multistream_encode(PointerByReference st, ShortByReference pcm, int frame_size, Pointer data, int max_data_bytes);

    int opus_multistream_encode_float(PointerByReference st, float pcm[], int frame_size, ByteBuffer data, int max_data_bytes);

    int opus_multistream_encode_float(PointerByReference st, FloatByReference pcm, int frame_size, Pointer data, int max_data_bytes);

    void opus_multistream_encoder_destroy(PointerByReference st);

    int opus_multistream_encoder_ctl(PointerByReference st, int request, Object... varargs);

    int opus_multistream_decoder_get_size(int streams, int coupled_streams);

    PointerByReference opus_multistream_decoder_create(int Fs, int channels, int streams, int coupled_streams, byte mapping[], IntBuffer error);

    int opus_multistream_decoder_init(PointerByReference st, int Fs, int channels, int streams, int coupled_streams, byte mapping[]);

    int opus_multistream_decoder_init(PointerByReference st, int Fs, int channels, int streams, int coupled_streams, Pointer mapping);

    int opus_multistream_decode(PointerByReference st, byte data[], int len, ShortBuffer pcm, int frame_size, int decode_fec);

    int opus_multistream_decode(PointerByReference st, Pointer data, int len, ShortByReference pcm, int frame_size, int decode_fec);

    int opus_multistream_decode_float(PointerByReference st, byte data[], int len, FloatBuffer pcm, int frame_size, int decode_fec);

    int opus_multistream_decode_float(PointerByReference st, Pointer data, int len, FloatByReference pcm, int frame_size, int decode_fec);

    int opus_multistream_decoder_ctl(PointerByReference st, int request, Object... varargs);

    void opus_multistream_decoder_destroy(PointerByReference st);
    public static class OpusMSEncoder extends PointerType {
        public OpusMSEncoder(Pointer address) {
            super(address);
        }
        public OpusMSEncoder() {
            super();
        }
    };
    public static class OpusMSDecoder extends PointerType {
        public OpusMSDecoder(Pointer address) {
            super(address);
        }
        public OpusMSDecoder() {
            super();
        }
    };

    PointerByReference opus_custom_mode_create(int Fs, int frame_size, IntBuffer error);

    void opus_custom_mode_destroy(PointerByReference mode);

    int opus_custom_encoder_get_size(PointerByReference mode, int channels);

    PointerByReference opus_custom_encoder_create(PointerByReference mode, int channels, IntBuffer error);

    PointerByReference opus_custom_encoder_create(PointerByReference mode, int channels, IntByReference error);

    void opus_custom_encoder_destroy(PointerByReference st);

    int opus_custom_encode_float(PointerByReference st, float pcm[], int frame_size, ByteBuffer compressed, int maxCompressedBytes);

    int opus_custom_encode_float(PointerByReference st, FloatByReference pcm, int frame_size, Pointer compressed, int maxCompressedBytes);

    int opus_custom_encode(PointerByReference st, ShortBuffer pcm, int frame_size, ByteBuffer compressed, int maxCompressedBytes);

    int opus_custom_encode(PointerByReference st, ShortByReference pcm, int frame_size, Pointer compressed, int maxCompressedBytes);

    int opus_custom_encoder_ctl(PointerByReference st, int request, Object... varargs);

    int opus_custom_decoder_get_size(PointerByReference mode, int channels);

    int opus_custom_decoder_init(PointerByReference st, PointerByReference mode, int channels);

    PointerByReference opus_custom_decoder_create(PointerByReference mode, int channels, IntBuffer error);

    PointerByReference opus_custom_decoder_create(PointerByReference mode, int channels, IntByReference error);

    void opus_custom_decoder_destroy(PointerByReference st);

    int opus_custom_decode_float(PointerByReference st, byte data[], int len, FloatBuffer pcm, int frame_size);

    int opus_custom_decode_float(PointerByReference st, Pointer data, int len, FloatByReference pcm, int frame_size);

    int opus_custom_decode(PointerByReference st, byte data[], int len, ShortBuffer pcm, int frame_size);

    int opus_custom_decode(PointerByReference st, Pointer data, int len, ShortByReference pcm, int frame_size);

    int opus_custom_decoder_ctl(PointerByReference st, int request, Object... varargs);
    public static class OpusCustomDecoder extends PointerType {
        public OpusCustomDecoder(Pointer address) {
            super(address);
        }
        public OpusCustomDecoder() {
            super();
        }
    };
    public static class OpusCustomEncoder extends PointerType {
        public OpusCustomEncoder(Pointer address) {
            super(address);
        }
        public OpusCustomEncoder() {
            super();
        }
    };
    public static class OpusCustomMode extends PointerType {
        public OpusCustomMode(Pointer address) {
            super(address);
        }
        public OpusCustomMode() {
            super();
        }
    };
}