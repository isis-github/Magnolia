<!--
 -  Copyright 2005 obinary ag.  All rights reserved.
 -  See license distributed with this file and available
 -  online at http://www.magnolia.info/dms-license.html
 -->

<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:cms="urn:jsptld:cms-taglib"
    xmlns:cmsu="urn:jsptld:cms-util-taglib" xmlns:c="urn:jsptld:http://java.sun.com/jsp/jstl/core">
    <cms:adminOnly>
        <div style="clear:both;">
            <cms:newBar contentNodeCollectionName="mainColumnParagraphs"
                paragraph="samplesTextImage,samplesDownload,samplesLink,samplesTable,dmsSearchResult"/>
        </div>
    </cms:adminOnly>
</jsp:root>