/* ###################################
### adminCentral.js
################################### */

function mgnlAdminCentralResize()
	{
	var divExtractTree=document.getElementById("mgnlAdminCentral_ExtractTreeDiv");
	var divExtractNonTree=document.getElementById("mgnlAdminCentral_ExtractNonTreeDiv");
	var divButtons=document.getElementById("mgnlAdminCentral_ButtonsDiv");

	if (divExtractTree && divButtons)
		{
		if (navigator.userAgent.toLowerCase().indexOf("safari")!=-1)
			{
			divExtractNonTree.style.display="block";
			}


		var sizeObj=mgnlGetWindowSize();
		//80 is top position of divTrees
		var h=sizeObj.h-80-20;
		//20 is left position of divTrees
		var w=sizeObj.w-200-20;


		//todo: to be tested!
		var agent=navigator.userAgent.toLowerCase();
		if (agent.indexOf("msie")!=-1) divButtons.style.height=h-29;
		else divButtons.style.height=h-37;

		divExtractTree.style.width=w;
		divExtractTree.style.height=h;
		var iFrameTree=document.getElementById("mgnlAdminCentral_ExtractTreeIFrame");
		iFrameTree.style.height=h;

		divExtractNonTree.style.width=w;
		divExtractNonTree.style.height=h-30;
		var iFrameNonTree=document.getElementById("mgnlAdminCentral_ExtractNonTreeIFrame");
		iFrameNonTree.style.height=h-30;
		}
	}

function mgnlAdminCentralSwitchExtractTree(name,href)
	{
	var divExtractTree=document.getElementById("mgnlAdminCentral_ExtractTreeDiv");
	var divExtractNonTree=document.getElementById("mgnlAdminCentral_ExtractNonTreeDiv");

	divExtractTree.style.visibility="hidden";
	divExtractNonTree.style.visibility="hidden";

	if (navigator.userAgent.toLowerCase().indexOf("safari")==-1)
		{
		divExtractTree.style.display="block";
		divExtractNonTree.style.display="none";
		}


    var iFrameDoc=mgnlGetIFrameDocument('mgnlAdminCentral_ExtractTreeIFrame');
	if (!href) href="/.magnolia/adminCentral/extractTree.html";

	var divSuper=iFrameDoc.getElementById("mgnlTree_DivSuper");
	if (divSuper) divSuper.style.display="none";

	href=mgnlAddParameter(href,"mgnlCK",mgnlGetCacheKiller());
	href=mgnlAddParameter(href,"name", name);
	iFrameDoc.location.href = contextPath + href;

	divExtractTree.style.visibility="visible";
	}

function mgnlAdminCentralSwitchExtractNonTree(href, external)
	{
	external = external == null? false : external;
		
	var divExtractTree=document.getElementById("mgnlAdminCentral_ExtractTreeDiv");
	var divExtractNonTree=document.getElementById("mgnlAdminCentral_ExtractNonTreeDiv");

	divExtractTree.style.visibility="hidden";
	divExtractNonTree.style.visibility="hidden";

	if (navigator.userAgent.toLowerCase().indexOf("safari")==-1)
		{
		divExtractTree.style.display="none";
		divExtractNonTree.style.display="block";
		}

	divExtractNonTree.style.visibility="visible";

	var iframe = document.getElementById('mgnlAdminCentral_ExtractNonTreeIFrame');
	if( ! external){
		href = contextPath + href;
		href=mgnlAddParameter(href,"mgnlCK",mgnlGetCacheKiller());
	}
	
	iframe.src = href;
	}




