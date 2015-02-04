# README #

Separate app with API 10 to test Bluetooth protocol


# eMotoBTService Class

    //Class Constants
    public final static byte[] PREAMBLE = {(byte)0xEC,(byte)0xDF};
    public final static byte PREAMBLE0 = (byte)0xEC;
    public final static byte PREAMBLE1 = (byte)0xDF;
    public final static byte GET_STATUS = (byte)0xA5;
    public final static byte RTS_IMAGE = (byte)0x4B;
    public final static byte ACK_IMAGE_INFO = (byte)0x6B;
    public final static byte ACK_IMAGE_DATA = (byte)0x4A;
    public final static byte NACK_RTS = (byte)0x9E;
    public final static String eMotoCellBTName = "HC-06";

    //Constructor
    public eMotoBTService(Context context);

    //Methods
    public static boolean initiateBT ()
    public int getServiceState ()

    public void sendBytes (byte[] bytes)
    public boolean sendEMotoPacket(byte command,int transaction, byte[] payload)
    public boolean sendImageData(int transaction, byte[] imageData)
