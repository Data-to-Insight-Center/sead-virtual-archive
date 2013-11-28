A number of sample images were created from a photograph taken in Nikon RAW format.  These samples were created as follows:

1) Photograph captured in Nikon RAW format, using a Nikon D800 SLR
2) RAW photo imported into Apple Aperture 3.4.3
3) IPTC, Spatial, and EXIF metadata were added to the photo.
4) Four derivative images were created
5) Metadata exported as an XMP side car file
5) Metadata exported as plain text

The four derivative images are:
1) sample-iptc-geo-sidecar.NEF: This is the original RAW photograph, with its metadata exported in a "sidecar" file: sample-iptc-exif-geo-sidecar.XMP
2) sample-iptc-exif-geo.jpg: A JPEG derivative, scaled down to 50% of the original size, and to 72 dpi
3) sample-iptc-exif-geo.png: A PNG derivative, scaled down to 50% of the original size, and to 72 dpi
4) sample-iptc-exif-geo.tiff: A TIFF derivative, scaled down to 50% of the original size, and to 72 dpi

The original RAW photo is available as: sample-iptc-exif-geo.NEF

It doesn't appear that all the metadata is available in all of the derivative images:
1) JPEG contains limited EXIF info, and no geocoding, and no IPTC
2) PNG contains no EXIF, and no geocoding, and no IPTC

This might be a shortcoming of Apple Aperture. That is, it didn't create the derivative images properly, with all of their attendant metadata.  The TIFF image seems to contain all the metadata.

Observations: the XMP sidecar contains RDF XML, and it seems to contain all the metadata.
