insert into metadata_type values('md:1',
'http://purl.org/dc/terms/','title'
);
insert into metadata_type values('md:2',
'http://purl.org/dc/terms/','creator'
);
insert into metadata_type values('md:3',
'http://purl.org/dc/terms/','identifier'
);
insert into metadata_type values('md:4',
'http://purl.org/dc/terms/','hasPart'
);
insert into metadata_type values('md:5',
'http://purl.org/dc/elements/1.1/','format'
);
insert into metadata_type values('md:6',
'http://purl.org/dc/terms/','SizeOrDuration'
);
insert into metadata_type values('md:7',
'http://purl.org/dc/terms/','issued'
);
insert into metadata_type values('md:8',
'http://purl.org/dc/terms/','mediator'
);
insert into metadata_type values('md:9',
'http://purl.org/dc/terms/','Location'
);
insert into metadata_type values('md:10',
'http://purl.org/dc/terms/','abstract'
);
insert into metadata_type values('md:11',
'http://purl.org/dc/terms/','source'
);
insert into metadata_type values('md:12',
'http://www.loc.gov/METS/','FLocat'
);
insert into metadata_type values('md:13',
'http://www.w3.org/ns/prov#','wasRevisionOf'
);

insert into metadata_type values('md:14',
'http://purl.org/dc/terms/','type'
);

insert into relation_type values('rl:1',
'http://www.openarchives.org/ore/terms/','describes'
);

insert into relation_type values('rl:2',
'http://purl.org/dc/terms/','publisher'
);

insert into relation_type values('rl:3',
'http://purl.org/pav/','curatedBy'
);

insert into base_entity values('state:1', 'PO',
'2014-01-01 00:00:00', '2014-01-01 00:00:00');

insert into state values('state:1',
'PO','PublishedObject'
);

insert into base_entity values('state:2', 'CuO',
'2014-05-18 00:21:11', '2014-05-18 00:21:11');

insert into state values('state:2',
'CuO','CurationObject'
);

insert into base_entity values('state:3', 'CO',
'2014-05-18 00:21:11', '2014-05-18 00:21:11');

insert into state values('state:3',
'CO','CapturedObject'
);

insert into base_entity values('state:4', 'OO',
'2014-12-10 00:21:11', '2014-12-10 00:21:11');

insert into state values('state:4',
'OO','OtherObject'
);

insert into repository values(
  'repo:1',
  'IU SDA',
  'HPSS',
  'Indiana University'
);

insert into repository values(
  'repo:2',
  'IU Scholarworks',
  'dspace',
  'Indiana University'
);

insert into repository values(
  'repo:3',
  'Ideals',
  'dspace',
  'UIUC'
);

insert into data_identifier_type values(
    'id:1',
    'doi',
     'http://dx.doi.org'
);

insert into data_identifier_type values(
    'id:2',
    'ark',
     'http://n2t.net/ark'
);

insert into data_identifier_type values(
    'id:3',
    'medici',
     'http://nced.ncsa.illinois.edu'
);

insert into data_identifier_type values(
    'id:4',
    'dpnobjectid',
     'DPN Object ID'
);

insert into data_identifier_type values(
    'id:5',
    'dataone',
     'http://seadva.d2i.indiana.edu'
);

insert into data_identifier_type values(
    'id:6',
    'storage_format',
     'Storage Format'
);

insert into data_identifier_type values(
    'id:7',
    'sda_location',
     'SDA Tarfile Location '
);


insert into role_type values(
    'role:1',
    'Curator',
    'Curator at an IR'
);

insert into role_type values(
    'role:2',
    'Researcher',
    'Scientist/Researcher'
);


insert into role_type values(
    'role:3',
    'Administrator',
    'Tech Administrator'
);


insert into role_type values(
    'role:4',
    'Repository Representative',
    'Represents IR, but not necessarily a Curator'
);

insert into profile_type values(
    'profile:1',
    'vivo',
    'http://sead-data.net/'
);



insert into metadata_type values('md:31', 'http://cet.ncsa.uiuc.edu/2007/annotation/', 'hasAnnotation');
insert into metadata_type values('md:32', 'http://cet.ncsa.uiuc.edu/2007/metadata/Extractor/', 'WmsLayerName');
insert into metadata_type values('md:33', 'http://cet.ncsa.uiuc.edu/2007/metadata/Extractor/', 'WmsLayerUrl');
insert into metadata_type values('md:34', 'http://cet.ncsa.uiuc.edu/2007/metadata/Extractor/', 'WmsServiceUrl');
insert into metadata_type values('md:35', 'http://cet.ncsa.uiuc.edu/2007/metadata/Medici/', 'original');
insert into metadata_type values('md:36', 'http://cet.ncsa.uiuc.edu/2007/mmdb/', 'describes');
insert into metadata_type values('md:37', 'http://cet.ncsa.uiuc.edu/2007/mmdb/', 'duplicates');
insert into metadata_type values('md:38', 'http://cet.ncsa.uiuc.edu/2007/mmdb/', 'hasSource');
insert into metadata_type values('md:39', 'http://cet.ncsa.uiuc.edu/2007/mmdb/', 'isDescribedBy');
insert into metadata_type values('md:40', 'http://cet.ncsa.uiuc.edu/2007/mmdb/', 'isDownloadedBy');
insert into metadata_type values('md:41', 'http://cet.ncsa.uiuc.edu/2007/mmdb/', 'isLikedDislikedBy');
insert into metadata_type values('md:42', 'http://cet.ncsa.uiuc.edu/2007/mmdb/', 'isViewedBy');
insert into metadata_type values('md:43', 'http://cet.ncsa.uiuc.edu/2007/mmdb/', 'relatesTo');
insert into metadata_type values('md:44', 'http://example.com/ODM/', 'characterizes');
insert into metadata_type values('md:45', 'http://example.com/ODM/', 'fromSite');
insert into metadata_type values('md:46', 'http://example.com/ODM/', 'hasQualityLevel');
insert into metadata_type values('md:48', 'http://purl.org/dc/elements/1.1/', 'date');
insert into metadata_type values('md:49', 'http://purl.org/dc/elements/1.1/', 'description');
insert into metadata_type values('md:50', 'http://purl.org/dc/terms/', 'alternative');
insert into metadata_type values('md:51', 'http://purl.org/dc/terms/', 'audience');
insert into metadata_type values('md:52', 'http://purl.org/dc/terms/', 'bibliographicCitation');
insert into metadata_type values('md:53', 'http://purl.org/dc/terms/', 'coverage');
insert into metadata_type values('md:54', 'http://purl.org/dc/terms/', 'dateCopyrighted');
insert into metadata_type values('md:55', 'http://purl.org/dc/terms/', 'description');
insert into metadata_type values('md:56', 'http://purl.org/dc/terms/', 'isPartOf');
insert into metadata_type values('md:57', 'http://purl.org/dc/terms/', 'isReplacedBy');
insert into metadata_type values('md:58', 'http://purl.org/dc/terms/', 'license');
insert into metadata_type values('md:59', 'http://purl.org/dc/terms/', 'publisher');
insert into metadata_type values('md:60', 'http://purl.org/dc/terms/', 'references');
insert into metadata_type values('md:61', 'http://purl.org/dc/terms/', 'rights');
insert into metadata_type values('md:62', 'http://purl.org/dc/terms/', 'rightsHolder');
insert into metadata_type values('md:63', 'http://purl.org/dc/terms/', 'subject');
insert into metadata_type values('md:64', 'http://purl.org/dc/terms/', 'temporal');
insert into metadata_type values('md:65', 'http://purl.org/vocab/frbr/core#', 'embodimentOf');
insert into metadata_type values('md:66', 'http://sead-data.net/terms/', 'hasDataMaturityLevel');
insert into metadata_type values('md:67', 'http://sead-data.net/terms/', 'hasSHA1Digest');
insert into metadata_type values('md:68', 'http://sead-data.net/terms/odm/', 'characterizes');
insert into metadata_type values('md:69', 'http://sead-data.net/terms/odm/', 'dataquality');
insert into metadata_type values('md:70', 'http://sead-data.net/terms/odm/', 'location');
insert into metadata_type values('md:71', 'http://sead-data.net/terms/odm/', 'method');
insert into metadata_type values('md:72', 'http://sead-data.net/terms/odm/', 'QualityControlLevel');
insert into metadata_type values('md:73', 'http://sead-data.net/terms/', 'ProposedForPublication');
insert into metadata_type values('md:74', 'http://www.holygoat.co.uk/owl/redwood/0.1/tags/', 'taggedWithTag');
insert into metadata_type values('md:75', 'http://www.linkedearth.org/RSIV/ns#', 'hasLevelOfProcessing');
insert into metadata_type values('md:77', 'http://www.w3.org/2000/01/rdf-schema#', 'label');
insert into metadata_type values('md:78', 'http://www.w3.org/ns/prov#', 'hadPrimarySource');
insert into metadata_type values('md:79', 'http://www.w3.org/ns/prov/#', 'hadRevision');
insert into metadata_type values('md:81', 'tag:tupeloproject.org,2006:/2.0/files/', 'hasName');
insert into metadata_type values('md:82', 'tag:tupeloproject.org,2006:/2.0/gis/', 'hasGeoPoint');
insert into metadata_type values('md:83', 'http://seadva.org/terms/','replica');
insert into metadata_type values('md:84', 'http://purl.org/dc/elements/1.1/', 'creator');
insert into metadata_type values('md:85', 'http://sead-data.net/vocab/', 'hasComment');
insert into metadata_type values('md:86', 'http://sead-data.net/vocab/', 'hasGeoPoint');
