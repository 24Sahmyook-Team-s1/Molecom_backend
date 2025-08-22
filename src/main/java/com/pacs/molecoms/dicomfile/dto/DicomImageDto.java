package com.pacs.molecoms.dicomfile.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DicomImageDto {

    // ===== Keys & UIDs =====
    @Schema(description = "IMAGE.STUDYKEY")  private Long studyKey;
    @Schema(description = "IMAGE.SERIESKEY") private Long seriesKey;
    @Schema(description = "IMAGE.IMAGEKEY")  private Long imageKey;
    @Schema(description = "IMAGE.STUDYINSUID")   private String studyInsUid;
    @Schema(description = "IMAGE.SERIESINSUID")  private String seriesInsUid;
    @Schema(description = "IMAGE.SOPINSTANCEUID") private String sopInstanceUid;
    @Schema(description = "IMAGE.SOPCLASSUID")    private String sopClassUid;

    // ===== File path =====
    private String path;    // IMAGE.PATH
    private String fname;   // IMAGE.FNAME

    // ===== Index & Sequence =====
    private Integer seriesNumber;  // SERIESNUMBER
    private Integer instanceNum;   // INSTANCENUM
    private Integer curSeqNum;     // CURSEQNUM

    // ===== Window/Level =====
    private String window; // WINDOW
    private String lev;    // LEV

    // ===== Content/Acquisition datetime (원문 포맷 문자열) =====
    private String contentDate; // CONTENTDATE YYYYMMDD
    private String contentTime; // CONTENTTIME HHmmss(.SSS)
    private String acqDate;     // ACQDATE
    private String acqTime;     // ACQTIME

    // ===== Study/Position & Types =====
    private String studyId;       // STUDYID
    private String viewPosition;  // VIEWPOSITION
    private String laterality;    // LATERALITY
    private String imageType;     // IMAGETYPE

    // ===== Free text =====
    private String fmxData;         // FMXDATA
    private String imageComments;   // IMAGECOMMENTS
    private String additionalDesc;  // ADDITIONALDESC

    // ===== Geometry =====
    private String imageOrientation; // IMAGEORIENTATION (원본문자열)
    private String imagePosition;    // IMAGEPOSITION
    private String pixelSpacing;     // PIXELSPACING
    private Integer pixelRows;       // PIXELROWS
    private Integer pixelColumns;    // PIXELCOLUMNS

    // ===== Pixel format =====
    private Integer bitsAllocated;              // BITSALLOCATED
    private String specificCharacterSet;        // SPECIFICCHARACTERSET
    private String transferSyntaxUid;           // TRANSFERSYNTAXUID
    private String sourceApplicationEntityTitle;// SOURCEAPPLICATIONENTITYTITLE
    private String lossyImageCompression;       // LOSSYIMAGECOMPRESSION
    private Integer samplePerPixel;             // SAMPLEPERPIXEL
    private String photometricInterpretation;   // PHOTOMETRICINTERPRETATION
    private Integer bitsStored;                 // BITSSTORED
    private Integer highBit;                    // HIGHBIT
    private Integer pixelRepresentation;        // PIXELREPRESENTATION
    private Integer planarConfiguration;        // PLANARCONFIGURATION

    // ===== Frames & status =====
    private Integer frameCnt;     // FRAMECNT
    private Integer geomStatus;   // GEOMSTATUS
    private Integer archStatus;   // ARCHSTATUS
    private String archPath;      // ARCHPATH

    // ===== Flags =====
    private Integer delFlag;      // DELFLAG
    private Integer verifyFlag;   // VERIFYFLAG
    private Integer hideFlag;     // HIDEFLAG
    private Integer keyFlag;      // KEYFLAG
    private Integer compStatus;   // COMPSTATUS

    // ===== Presentation/LUT =====
    private String presentationStateData; // PRESENTATIONSTATEDATA
    private BigDecimal sharpenValue;      // SHARPENVALUE
    private String lutData;               // LUTDATA

    // ===== Sizes (bytes) =====
    private Long imageSize;  // IMAGESIZE
    private Long compSize;   // COMPSIZE

    // ===== Movie/video =====
    private String movPath;     // MOVPATH
    private String movFname;    // MOVFNAME
    private Integer movieFlag;  // MOVIEFLAG
    private String codecType;   // CODECTYPE
    private BigDecimal frameRate;// FRAMERATE
    private BigDecimal frameTime;// FRAMETIME

    // ===== Recording window =====
    private String recStartDate; // RECSTARTDATE
    private String recStartTime; // RECSTARTTIME
    private String recEndDate;   // RECENDDATE
    private String recEndTime;   // RECENDTIME

    // ===== Storage IDs =====
    private String movStStorageId; // MOVSTSTORAGEID
    private String ltStorageId;    // LTSTORAGEID
    private String stStorageId;    // STSTORAGEID
    private String webStorageId;   // WEBSTORAGEID

    // ===== Audit =====
    private String insertDate; // INSERTDATE
    private String insertTime; // INSERTTIME
    private String inserted;   // INSERTED
    private String updated;    // UPDATED
    private String hospitalId; // HOSPITALID

    // ===== Reserved =====
    private String reserved1;  private String reserved2;  private String reserved3;  private String reserved4;  private String reserved5;
    private String reserved6;  private String reserved7;  private String reserved8;  private String reserved9;  private String reserved10;

    // ===== Additional alias =====
    private String photometric;            // PHOTOMETRIC
    private String patientOrientation;     // PATIENTORIENTATION
    private String presentationLUTShape;   // PRESENTATIONLUTSHAPE
    private String instanceCreationDate;   // INSTANCECREATIONDATE
    private String instanceCreationTime;   // INSTANCECREATIONTIME
    private String sourceAETitle;          // SOURCEAETITLE

    // ===== AI =====
    private BigDecimal aiScore;       // AI_SCORE
    private Integer aiFindingCount;   // AI_FINDING_COUNT
}
