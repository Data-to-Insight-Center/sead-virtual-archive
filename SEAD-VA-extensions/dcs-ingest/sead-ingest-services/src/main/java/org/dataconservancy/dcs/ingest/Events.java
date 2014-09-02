package org.dataconservancy.dcs.ingest;
public interface Events {

    public static final String DOI_ID_ASSIGNMENT = "doi.assignment";
    public static final String DOI_ID_UPDATION = "doi.updation";
    public static final String VIRUS_SCAN = "virus.scan";
    public static final String ID_ASSIGNMENT = "identifier.assignment";

    /**
     * Generation of FGDC and OAI-ORE minimally based on available metadata for a collection
     */
    public static final String METADATA_GENERATION = "metadata.generation";

    /**
     * File download by users
     */
    public static final String FILE_READ = "file.read";

    /**
     * File metadata read by users
     */
    public static final String FILEMETADATA_READ = "file.metadataread";
    public static final String INGEST_SUCCESS = "ingest.complete";
    /**
     * File download by DataONE users
     */
    public static final String FILE_D1READ = "file.d1read";

    /**
     * File replicate by DataONE users
     */
    public static final String FILE_D1REPLICATE = "file.d1replicate";

    /**
     * File metadata read by DataONE users
     */
    public static final String FILEMETADATA_D1READ = "file.d1metadataread";
    public static final String ARCHIVE = "archive";

    public static final String COLD_COPY = "coldcopy";
    public static final String MATCH_MAKING = "matchmaking";
    public static final String REGISTRY_INGEST = "registry.entry";
    public static final String TARBAG = "tarbag";
    public static final String MSGNR = "messenger.registry";
}
