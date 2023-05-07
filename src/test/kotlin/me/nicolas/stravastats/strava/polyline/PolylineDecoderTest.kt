package me.nicolas.stravastats.strava.polyline

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

class PolylineDecoderTest {

    @Test
    fun decode() {
        // GIVEN
        val polyline =
            "ex|dHd_uH@LFLLDTIJYBSASKa@AMIg@]c@WYGC[BUCQSk@eAWeAECE?MH_AnAq@j@mBhAYJWNgAdAuF|DmBxAqAxA_CbD{FlHe@x@?NBPZ`@JBJ?RKbAaAVEPBXZhBfDz@xApCpBPRnEtGV^Nf@Lv@Dh@BlBF\\DnBH\\f@dBD`@BvBf@dGJt@?n@Ft@PfB@n@F`@TpD?pBB`BL~@^`FAhAGx@J|BN`BFzBIrA]jBgAlDIj@?xAPdAEdBKd@UZWTk@V?RG\\JK@H@EBA@FQl@OjAS|@?REVk@vCg@tB_AxCc@fAk@jBgCjHg@rAQ`AQx@Gb@o@bD_AlGMd@a@n@kBjCI`@O~CWbCk@jDg@`BGHCAcDuA}Aw@sAm@]I}CuA]IeBk@cJ_Dm@Ow@EsFh@gNdCeCP{@EkPmBoAKcC[c@MUQOQwDmHc@cA_D_Ko@eBy@aB_AcCYm@_@cA{ByH_AyCkAsCcHgMoAcBqEyEKQ_AsCKIq@[QQQa@GECQOUe@kAUYYSIWg@[EIs@a@SUIKA]OGCOS[Qi@UYUMMYBEPAHEFOHEDKHERYHwAJk@?WFg@TaAIy@D]^Sb@IBi@Ci@Ga@GUAWBMXODG?GJ_@NmA@w@Vo@Jq@@]BUIa@FOCKHs@CWIMBo@EQMI?GGCCWNGB]\\ILQLg@^]LSVu@Le@FOF_APIt@QRBLAVHLHTNPDh@Tl@l@dC|A\\NNP`An@PNf@PTPVHLRTPX@@AEOIo@_@{@WSq@KKUWSDNNFNAZNLPd@ZPVH`@R`@DREZ@HJPL^Ql@S~@AVDND^?XJP@t@CXIPGDK?ID@NIb@kApCWbA?tALdA?d@Kn@?d@Nx@?j@@J`@XPX@PMJ?JRRBJFr@d@f@r@j@LTLb@FFL@XPF?LFNAB@@Dp@h@@HLLRHLPJBb@CFRPT@HCHR^BPJVH\\A?Gb@Yz@CTQh@C\\a@xADZUd@CNMPELAF?ZSl@Iv@Sj@I^?LDHAHIPARSf@SJMROBYWoAi@UO]]w@e@k@g@o@c@]Om@o@i@UQQuAcBy@w@QYQUWOICUA]@MMQ_@DEFFBAHGF]P_@z@aD|AcHHS@CCAEDEX_@@UGIGCIPcAP_@H]HUASBK@g@HY@]FMDSh@}AF[HK?Gj@iBF_@@YFSHk@Ta@Tw@Dc@\\uA?e@Pm@HMHu@\\_AD_@FK@c@x@kC[[u@Wq@_@GOEG_@SUYMEk@[i@a@EGc@Mc@e@aB_Ae@UWSACDIEO@q@UWg@WGAm@[SGU?SRGCGAIDOC[U[KOWOIQYESMGIKUCMGGKWQGSSOIMMGEEQIIKe@K{@}@Mc@IE?IGGEWDYCWh@ATCTGH@ZGi@Ju@AOGg@Ii@_@GIUGCGHW@EAEBONOVBTHVc@DQHS@QDEBQCSEIDYDSJGDSHOLMBMROBGL[He@POVi@@QJe@V]DKDO@g@?MEW\\c@Fa@Xs@@ILQFCLSBIASDEFaAHM@YFE?ITIDIBWJ]Aq@DM?[MKL{AFO@Y^OWS[OCEAcAE]EMEAAIWCGMAK@IGq@ISSIYUOCe@QSQO_@[UMYFWNUD]EK?YS_@@gAJc@FIB]LQTq@CS@QGGCC@Ca@GOOKA][Q_@Um@AKFMAOK[?YCK?QFGNa@T[DYHG?WCO@UTs@COA_@DECEMEYo@EW@G\\c@TOHKLG^WR??BVFNFh@@ZOd@IBCLBHH@fACf@@^CzD@jAGbC?BFFT?nBN`CBjBHd@A\\BTA`CPbA@dCN\\ALBHDAh@D`@Qn@KdABRJjCCNOb@DJAPS\\DH@LCZ?VKNx@M^ABBFL?THLDNAPFf@Lj@Xj@Hh@Zn@BNAp@EJGZBFAL@PDFBVOT?FPf@@JCJHL?NFHDVJTBRCTGFa@JMNENA\\DNZPDSp@UZCPIPWHAVF`@Ap@s@r@UJSVQpA[f@SXFX?HCV[DAJ@JED@Bn@KxAwAbGSl@?DJRDTF?HKJ@NNXNRTRDPFtAp@dAb@lBrAXJD`@Zp@@v@ANLRI\\AX@Lf@f@JDFDVj@d@b@P@FFPZNJJBJ?HDPf@^ZBFNl@F^V|@~@pAL\\^ZZh@XZR\\BRRDd@b@HJPFBE^Kb@AJSPOLCJGJ?JKb@KZSVAl@I\\A|@e@HALUFAF?^VVFHAJGF?F@NAXFFHHZLVHBRNh@HNCN?ZGXDNCNHV?PID@LCTFRCP@n@ARK^?j@EfAWJJTHRLRBJGHIB[HILBLUBO@@GRMDKNGASSUYc@u@o@y@c@s@CSIWcAcBQ_@?GHYLEJY`@a@Rk@PQFKFWd@gAd@{@b@oARUNg@Vg@`@m@Pc@X]Re@JK^o@f@OPQRe@BURQXi@REb@n@N\\NJHBJC?KMYCY@EFCrAEZBvA?tCM|@@NPNn@JXXtABVMrA?ZD\\Tj@NLDAlC{CnDaFnCqD|@eAh@c@`JqG^QTW|BqApAqA~CkDz@cBn@{Ar@oBHKJEHBZ`@Z@d@KPHFNTnC@`@Ep@B\\JZdAzAx@nBHh@ARETU\\WPYJqBd@W@WEUK[YGEI?ON[|@e@fAWLGEIGEKBW"

        // WHEN
        val points = PolylineDecoder().decode(polyline)

        // THEN
        assertEquals(994, points.size)
        assertEquals(Point(48.1576, -1.5872), points.first())
        assertEquals(Point(48.1576, -1.5872), points.last())
    }
}