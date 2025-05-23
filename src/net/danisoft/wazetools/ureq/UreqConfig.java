////////////////////////////////////////////////////////////////////////////////////////////////////
//
// UreqConfig.java
//
// UREQ Config
//
// First Release: ???/???? by Fulvio Mondini (https://danisoft.software/)
//       Revised: Mar/2025 Ported to Waze dslib.jar
//
////////////////////////////////////////////////////////////////////////////////////////////////////

package net.danisoft.wazetools.ureq;

import javax.servlet.http.Cookie;

import org.json.JSONObject;

/**
 * UREQ Config
 */
public class UreqConfig {

	public static final String UREQ_MANAGER_CFG_COOKIE_NAME = "UreqMgrCfg";

	/**
	 * Constructor
	 */
	public UreqConfig(Cookie[] cookiesArray) throws Exception {
		super();

		_set_defaults();
		_use_saved_cfg(cookiesArray);
	}

	/**
	 * Entry Params
	 */
	public class EntryParam {

		// Fields
		private String	_JKeyw;
		private String	_Descr;
		private boolean	_Value;

		// Getters
		public String	getJKeyw()	{ return this._JKeyw;	}
		public String	getDescr()	{ return this._Descr;	}
		public boolean	getValue()	{ return this._Value;	}

		// Setters
		public void setJKeyw(String jKeyw)	{ this._JKeyw = jKeyw;	}
		public void setDescr(String descr)	{ this._Descr = descr;	}
		public void setValue(boolean value)	{ this._Value = value;	}

		/**
		 * Constructor
		 */
		public EntryParam(String jKeyw, String descr, boolean value) {
			super();
			this._JKeyw = jKeyw;
			this._Descr = descr;
			this._Value = value;
		}
	}

	private EntryParam	_ShowLayerOpen;
	private EntryParam	_ShowLayerWork;
	private EntryParam	_ShowLayerInfo;
	private EntryParam	_ShowLayerRjct;
	private EntryParam	_ShowLayerRchk;
	private EntryParam	_ShowLayerDone;

	private EntryParam	_ShowLock1;
	private EntryParam	_ShowLock2;
	private EntryParam	_ShowLock3;
	private EntryParam	_ShowLock4;
	private EntryParam	_ShowLock5;
	private EntryParam	_ShowLock6;

	private EntryParam	_ShowReqAll;
	private EntryParam	_ShowReqMine;
	private EntryParam	_ShowReqOthers;
	private EntryParam	_ShowReqNew;

	private EntryParam	_ShowAreaAll;
	private EntryParam	_ShowAreaMine;

	private EntryParam	_UseWmeProd;
	private EntryParam	_UseWmeBeta;

	// Getters

	public final EntryParam	getLayerOpen()		{ return(this._ShowLayerOpen);	}
	public final EntryParam	getLayerWork()		{ return(this._ShowLayerWork);	}
	public final EntryParam	getLayerInfo()		{ return(this._ShowLayerInfo);	}
	public final EntryParam	getLayerRjct()		{ return(this._ShowLayerRjct);	}
	public final EntryParam	getLayerRchk()		{ return(this._ShowLayerRchk);	}
	public final EntryParam	getLayerDone()		{ return(this._ShowLayerDone);	}

	public final EntryParam	getLock1()			{ return(this._ShowLock1);		}
	public final EntryParam	getLock2()			{ return(this._ShowLock2);		}
	public final EntryParam	getLock3()			{ return(this._ShowLock3);		}
	public final EntryParam	getLock4()			{ return(this._ShowLock4);		}
	public final EntryParam	getLock5()			{ return(this._ShowLock5);		}
	public final EntryParam	getLock6()			{ return(this._ShowLock6);		}

	public final EntryParam	getReqAll()			{ return(this._ShowReqAll);		}
	public final EntryParam	getReqMine()		{ return(this._ShowReqMine);	}
	public final EntryParam	getReqOthers()		{ return(this._ShowReqOthers);	}
	public final EntryParam	getReqNew()			{ return(this._ShowReqNew);		}

	public final EntryParam	getAreaAll()		{ return(this._ShowAreaAll);	}
	public final EntryParam	getAreaMine()		{ return(this._ShowAreaMine);	}

	public final EntryParam	getWmeProd()		{ return(this._UseWmeProd);		}
	public final EntryParam	getWmeBeta()		{ return(this._UseWmeBeta);		}

	// Setters

	public void setLayerOpen(EntryParam layerOpen)		{ this._ShowLayerOpen = layerOpen;	}
	public void setLayerWork(EntryParam layerWork)		{ this._ShowLayerWork = layerWork;	}
	public void setLayerInfo(EntryParam layerInfo)		{ this._ShowLayerInfo = layerInfo;	}
	public void setLayerRjct(EntryParam layerRjct)		{ this._ShowLayerRjct = layerRjct;	}
	public void setLayerRchk(EntryParam layerRchk)		{ this._ShowLayerRchk = layerRchk;	}
	public void setLayerDone(EntryParam layerDone)		{ this._ShowLayerDone = layerDone;	}

	public void setLock1(EntryParam lock1)				{ this._ShowLock1 = lock1;			}
	public void setLock2(EntryParam lock2)				{ this._ShowLock2 = lock2;			}
	public void setLock3(EntryParam lock3)				{ this._ShowLock3 = lock3;			}
	public void setLock4(EntryParam lock4)				{ this._ShowLock4 = lock4;			}
	public void setLock5(EntryParam lock5)				{ this._ShowLock5 = lock5;			}
	public void setLock6(EntryParam lock6)				{ this._ShowLock6 = lock6;			}

	public void setReqAll(EntryParam reqAll)			{ this._ShowReqAll = reqAll;		}
	public void setReqMine(EntryParam reqMine)			{ this._ShowReqMine = reqMine;		}
	public void setReqOthers(EntryParam reqOthers)		{ this._ShowReqOthers = reqOthers;	}
	public void setReqNew(EntryParam reqNew)			{ this._ShowReqNew = reqNew;		}

	public void setAreaAll(EntryParam areaAll)			{ this._ShowAreaAll = areaAll;		}
	public void setAreaMine(EntryParam areaMine)		{ this._ShowAreaMine = areaMine;	}

	public void setWmeProd(EntryParam wmeProd)			{ this._UseWmeProd = wmeProd;		}
	public void setWmeBeta(EntryParam wmeBeta)			{ this._UseWmeBeta = wmeBeta;		}

	/**
	 * Create a JSONObject of all parameters
	 */
	public JSONObject getJsonCfg() {

		JSONObject jCfg = new JSONObject();

		try { jCfg.put(getLayerOpen().getJKeyw(),	getLayerOpen().getValue());	} catch (Exception pe) { }
		try { jCfg.put(getLayerWork().getJKeyw(),	getLayerWork().getValue());	} catch (Exception pe) { }
		try { jCfg.put(getLayerInfo().getJKeyw(),	getLayerInfo().getValue());	} catch (Exception pe) { }
		try { jCfg.put(getLayerRjct().getJKeyw(),	getLayerRjct().getValue());	} catch (Exception pe) { }
		try { jCfg.put(getLayerRchk().getJKeyw(),	getLayerRchk().getValue());	} catch (Exception pe) { }
		try { jCfg.put(getLayerDone().getJKeyw(),	getLayerDone().getValue());	} catch (Exception pe) { }

		try { jCfg.put(getLock1().getJKeyw(),		getLock1().getValue());		} catch (Exception pe) { }
		try { jCfg.put(getLock2().getJKeyw(),		getLock2().getValue());		} catch (Exception pe) { }
		try { jCfg.put(getLock3().getJKeyw(),		getLock3().getValue());		} catch (Exception pe) { }
		try { jCfg.put(getLock4().getJKeyw(),		getLock4().getValue());		} catch (Exception pe) { }
		try { jCfg.put(getLock5().getJKeyw(),		getLock5().getValue());		} catch (Exception pe) { }
		try { jCfg.put(getLock6().getJKeyw(),		getLock6().getValue());		} catch (Exception pe) { }

		try { jCfg.put(getReqAll().getJKeyw(),		getReqAll().getValue());	} catch (Exception pe) { }
		try { jCfg.put(getReqMine().getJKeyw(),		getReqMine().getValue());	} catch (Exception pe) { }
		try { jCfg.put(getReqOthers().getJKeyw(),	getReqOthers().getValue());	} catch (Exception pe) { }
		try { jCfg.put(getReqNew().getJKeyw(),		getReqNew().getValue());	} catch (Exception pe) { }

		try { jCfg.put(getAreaAll().getJKeyw(),		getAreaAll().getValue());	} catch (Exception pe) { }
		try { jCfg.put(getAreaMine().getJKeyw(),	getAreaMine().getValue());	} catch (Exception pe) { }

		try { jCfg.put(getWmeProd().getJKeyw(),		getWmeProd().getValue());	} catch (Exception pe) { }
		try { jCfg.put(getWmeBeta().getJKeyw(),		getWmeBeta().getValue());	} catch (Exception pe) { }

		return(jCfg);
	}

	/**
	 * Set start default values
	 */
	private void _set_defaults() {

		// ----------------------------	-----------------------	---------------------------------------	------
		//								JSON Keyword			Description								Value
		// ----------------------------	-----------------------	---------------------------------------	------

		setLayerOpen(	new EntryParam(	"jCfg-LayerShowOpen",	_req_status_descr(RequestStatus.OPEN),	true	));
		setLayerWork(	new EntryParam(	"jCfg-LayerShowWork",	_req_status_descr(RequestStatus.WORK),	true	));
		setLayerInfo(	new EntryParam(	"jCfg-LayerShowInfo",	_req_status_descr(RequestStatus.INFO),	true	));
		setLayerRjct(	new EntryParam(	"jCfg-LayerShowRjct",	_req_status_descr(RequestStatus.RJCT),	false	));
		setLayerRchk(	new EntryParam(	"jCfg-LayerShowRchk",	_req_status_descr(RequestStatus.RCHK),	true	));
		setLayerDone(	new EntryParam(	"jCfg-LayerShowDone",	_req_status_descr(RequestStatus.DONE),	false	));

		setLock1(		new EntryParam(	"jCfg-ShowLock1",		"Show requests of <b>L1</b> objects",	true	));
		setLock2(		new EntryParam(	"jCfg-ShowLock2",		"Show requests of <b>L2</b> objects",	true	));
		setLock3(		new EntryParam(	"jCfg-ShowLock3",		"Show requests of <b>L3</b> objects",	true	));
		setLock4(		new EntryParam(	"jCfg-ShowLock4",		"Show requests of <b>L4</b> objects",	true	));
		setLock5(		new EntryParam(	"jCfg-ShowLock5",		"Show requests of <b>L5</b> objects",	true	));
		setLock6(		new EntryParam(	"jCfg-ShowLock6",		"Show requests of <b>L6</b> objects",	true	));

		setReqAll(		new EntryParam(	"jCfg-ShowReqAll",		"Show ALL requests",					true	));
		setReqMine(		new EntryParam(	"jCfg-ShowReqMine",		"Only requests managed by me",			false	));
		setReqOthers(	new EntryParam(	"jCfg-ShowReqOthers",	"Only requests managed by others",		false	));
		setReqNew(		new EntryParam(	"jCfg-ShowReqNew",		"Only unmanaged requests (new)",		false	));

		setAreaAll(		new EntryParam(	"jCfg-ShowAreaAll",		"Show all requests",					true	));
		setAreaMine(	new EntryParam(	"jCfg-ShowAreaMine",	"Only show requests in my area",		false	));

		setWmeProd(		new EntryParam(	"jCfg-UseWmeProd",		"Open in WME production",				true	));
		setWmeBeta(		new EntryParam(	"jCfg-UseWmeBeta",		"Open in WME beta",						false	));
	}

	/**
	 * Put saved values (if exists) in objects
	 */
	private void _use_saved_cfg(Cookie[] cookiesArray) {

		JSONObject jCfg = new JSONObject();

		try {
			for (Cookie cookie : cookiesArray) {
				if (cookie.getName().equals(UREQ_MANAGER_CFG_COOKIE_NAME)) {
					jCfg = new JSONObject(java.net.URLDecoder.decode(cookie.getValue(), "UTF-8"));
					break;
				}
			}
		} catch (Exception e) { }

		try { getLayerOpen()	.setValue(jCfg.getBoolean("jCfg-LayerShowOpen"));	} catch (Exception se) { }
		try { getLayerWork()	.setValue(jCfg.getBoolean("jCfg-LayerShowWork"));	} catch (Exception se) { }
		try { getLayerInfo()	.setValue(jCfg.getBoolean("jCfg-LayerShowInfo"));	} catch (Exception se) { }
		try { getLayerRjct()	.setValue(jCfg.getBoolean("jCfg-LayerShowRjct"));	} catch (Exception se) { }
		try { getLayerRchk()	.setValue(jCfg.getBoolean("jCfg-LayerShowRchk"));	} catch (Exception se) { }
		try { getLayerDone()	.setValue(jCfg.getBoolean("jCfg-LayerShowDone"));	} catch (Exception se) { }

		try { getLock1()		.setValue(jCfg.getBoolean("jCfg-ShowLock1"));		} catch (Exception se) { }
		try { getLock2()		.setValue(jCfg.getBoolean("jCfg-ShowLock2"));		} catch (Exception se) { }
		try { getLock3()		.setValue(jCfg.getBoolean("jCfg-ShowLock3"));		} catch (Exception se) { }
		try { getLock4()		.setValue(jCfg.getBoolean("jCfg-ShowLock4"));		} catch (Exception se) { }
		try { getLock5()		.setValue(jCfg.getBoolean("jCfg-ShowLock5"));		} catch (Exception se) { }
		try { getLock6()		.setValue(jCfg.getBoolean("jCfg-ShowLock6"));		} catch (Exception se) { }

		try { getReqAll()		.setValue(jCfg.getBoolean("jCfg-ShowReqAll"));		} catch (Exception se) { }
		try { getReqMine()		.setValue(jCfg.getBoolean("jCfg-ShowReqMine"));		} catch (Exception se) { }
		try { getReqOthers()	.setValue(jCfg.getBoolean("jCfg-ShowReqOthers"));	} catch (Exception se) { }
		try { getReqNew()		.setValue(jCfg.getBoolean("jCfg-ShowReqNew"));		} catch (Exception se) { }

		try { getAreaAll()		.setValue(jCfg.getBoolean("jCfg-ShowAreaAll"));		} catch (Exception se) { }
		try { getAreaMine()		.setValue(jCfg.getBoolean("jCfg-ShowAreaMine"));	} catch (Exception se) { }

		try { getWmeProd()		.setValue(jCfg.getBoolean("jCfg-UseWmeProd"));		} catch (Exception se) { }
		try { getWmeBeta()		.setValue(jCfg.getBoolean("jCfg-UseWmeBeta"));		} catch (Exception se) { }
	}

	/**
	 * Get specific description for RequestStatus only
	 */
	private static String _req_status_descr(RequestStatus reqStatus) {
		return(
			RequestStatus.getColorizedSpan(reqStatus) + "&nbsp;&nbsp;Show <b>" + reqStatus.getDescr() + "</b> requests"
		);
	}

}
