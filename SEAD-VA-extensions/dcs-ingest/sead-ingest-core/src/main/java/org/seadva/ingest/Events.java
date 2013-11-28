package org.seadva.ingest;

public interface Events {


    public static final String ARCHIVE = "archive";

    public static final String COLD_COPY = "coldcopy";
    public static final String MATCH_MAKING = "matchmaking";
    public static final String BATCH = "batch";
    public static final String CHARACTERIZATION_FORMAT =
            "characterization.format";
    public static final String CHARACTERIZATION_METADATA =
            "characterization.metadata";
    public static final String DEPOSIT = "deposit";
    public static final String FILE_DOWNLOAD = "file.download";
    public static final String FILE_RESOLUTION_STAGED = "file.resolution";
    public static final String FILE_UPLOAD = "file.upload";
    public static final String FIXITY_DIGEST = "fixity.digest";
    public static final String ID_ASSIGNMENT = "identifier.assignment";
    public static final String DOI_ID_ASSIGNMENT = "doi.assignment";
    public static final String DOI_ID_UPDATION = "doi.updation";
    public static final String INGEST_START = "ingest.start";
    public static final String INGEST_SUCCESS = "ingest.complete";
    public static final String INGEST_FAIL = "ingest.fail";
    public static final String TRANSFORM = "transform";
    public static final String TRANSFORM_FAIL = "transform.fail";
    public static final String VIRUS_SCAN = "virus.scan";
    public static final String METADATA_GENERATION = "metadata.generation";
    public static final String FILE_READ = "file.read";
    public static final String FILEMETADATA_READ = "file.metadataread";
    public static final String FILE_D1READ = "file.d1read";
    public static final String FILE_D1REPLICATE = "file.d1replicate";
    public static final String FILEMETADATA_D1READ = "file.d1metadataread";

}
