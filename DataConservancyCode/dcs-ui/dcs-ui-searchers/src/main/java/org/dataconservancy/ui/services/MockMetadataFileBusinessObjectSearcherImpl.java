package org.dataconservancy.ui.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsRelation;
import org.dataconservancy.model.dcs.DcsRelationship;
import org.dataconservancy.model.dcs.DcsResourceIdentifier;
import org.dataconservancy.ui.model.Id;

public class MockMetadataFileBusinessObjectSearcherImpl extends MockFileBusinessObjectSearcher implements MetadataFileBusinessObjectSearcher {

    public MockMetadataFileBusinessObjectSearcherImpl(MockArchiveUtil archiveUtil) {
        super(archiveUtil);
    }

    @Override
    public DcsFile findMetadataFile(String business_id) {
        return super.findFile(business_id, Types.METADATA_FILE.name());
    }
}