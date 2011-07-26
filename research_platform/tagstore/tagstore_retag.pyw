ó
Ä9Nc        	   @   s0  d  d l  Z  d  d l Z d  d l m Z m Z d  d l m Z d  d l m Z d  d l	 m
 Z
 m Z m Z d  d l m Z m Z d  d l m Z d  d l m Z d  d	 l m Z d  d
 l m Z d  d l m Z d e j f d     YZ d e j f d     YZ e d k r#d Z e d  Z e j  d d d d d d e j  d d d d d d d d e j!   \ Z" Z# e$ Z% e$ Z& e$ Z' e( Z) e( Z* e" j+ r e, Z% n  e" j- rµe" j- Z- n d GHe j.   e  j/   e j0 e  j1  Z2 e2 j3 d   e2 j4 d!  e2 j5 e e2 e- e, e%  Z6 e2 j7   n  d"   Z8 d S(#   iÿÿÿÿN(   t   QtCoret   QtGui(   t   OptionParser(   t   ConfigWrapper(   t   EDateStampFormatt   EConflictTypet
   EFileEvent(   t   NameInConflictExceptiont   InodeShortageException(   t	   LogHelper(   t
   PathHelper(   t   Store(   t   TsConstants(   t   TagDialogControllert   ReTagControllerc           B   s   e  Z d  Z e e d  Z d   Z d   Z d   Z d   Z	 d   Z
 d   Z d   Z d	   Z d
   Z d   Z d   Z d   Z d   Z d   Z RS(   sO  
    object for calling the re-tag view.
    ************************
    MANDATORY parameters: 
    ************************
    * application -> the parent qt-application object ()for installing the translator properly
    * store_path -> absolute path to the store of the item to be retagged (TIP: use the PathHelper object to resolve a relative path.)
    * item_name -> the name of the item to be renamed (exactly how it is defined in the tagfile)

    ************************
    TIP: use the PathHelper object to resolve a relative path AND to extract the item name out of it. 
    ************************
    
    ************************
    OPTIONAL parameters:
    ************************
    * standalone_application -> default = False; set this to true if there
    * verbose -> set this to true for detailed output
    (DEVEL * retag_mode -> this application could even be used for a normal tagging procedure as well.)
    
    ************************
    IMPORTANT!!!
    ************************
    the start() method must be called in order to begin with the tagging procedure
    c         C   s@  t  j j |   d  |  _ d  |  _ d  |  _ d  |  _ d  |  _ | |  _	 t
 |  _ t |  |  _ | |  _ | |  _ t j |  _ | r t j |  _ n  t j |  _ t j |  _ t j |  _ t j |  _ t t  j j   j    d d !} t  j    |  _! |  j! j" d | d d  r%|  j j# |  j!  n  |  j$ d  |  _% g  |  _& g  |  _' g  |  _( g  |  _) g  |  _* t j+ |  _, i  |  _- x |  j, D] } |  j. |  |  j* j/ |  j$ d   |  j& j/ |  j$ d   |  j' j/ |  j$ d	   |  j( j/ |  j$ d
   |  j) j/ |  j$ d   qW|  j. |  j%  t0 j1 |  j  |  _ d  S(   Ni    i   t   ts_s   .qms   tsresources/t   ent
   navigationt   storaget   descriptionst
   categoriest   expired_items(2   R    t   QObjectt   __init__t   Nonet   _ReTagController__logt   _ReTagController__main_configt   _ReTagController__store_configt   _ReTagController__tag_dialogt   _ReTagController__storet   _ReTagController__retag_modet   Falset    _ReTagController__no_store_foundt   unicodet   _ReTagController__item_namet   _ReTagController__store_patht   _ReTagController__applicationt   loggingt   INFOt	   LOG_LEVELt   DEBUGR   t   DEFAULT_STORE_CONFIG_DIRt   STORE_CONFIG_DIRt   DEFAULT_STORE_CONFIG_FILENAMEt   STORE_CONFIG_FILE_NAMEt   DEFAULT_STORE_TAGS_FILENAMEt   STORE_TAGS_FILE_NAMEt!   DEFAULT_STORE_VOCABULARY_FILENAMEt   STORE_VOCABULARY_FILE_NAMEt   QLocalet   systemt   namet   QTranslatort   _ReTagController__translatort   loadt   installTranslatort   trUtf8t   CURRENT_LANGUAGEt   STORE_STORAGE_DIRSt   STORE_DESCRIBING_NAV_DIRSt   STORE_CATEGORIZING_NAV_DIRSt   STORE_EXPIRED_DIRSt   STORE_NAVIGATION_DIRSt   DEFAULT_SUPPORTED_LANGUAGESt   SUPPORTED_LANGUAGESt   _ReTagController__store_dictt   change_languaget   appendR	   t   get_app_logger(   t   selft   applicationt
   store_patht	   item_namet
   retag_modet   verboset   localet   lang(    (    sB   /home/vk/src/tagstore/research_platform/tagstore/tagstore_retag.pyR   :   sN    									"						c         C   s   |  j    d S(   sJ   
        call this method to actually start the tagging procedure
        N(   t$   _ReTagController__init_configuration(   RE   (    (    sB   /home/vk/src/tagstore/research_platform/tagstore/tagstore_retag.pyt   startr   s    c         C   s  |  j  j d  t t j  |  _ |  j d k rK |  j |  j d   d S|  j	 d k rt |  j |  j d   d St |  j	  |  _
 |  j   |  j j   |  _ |  j |  j  t |  j
 j   |  j	 |  j d |  j |  j d |  j |  j d |  j |  j |  j |  j |  j |  j |  j j    |  _ |  j j   |  j d k rèt |  j j   |  j j    |  j j!   |  j j    |  _ |  j j"   j# t$  |  j j% |  j t& j' d  |  j(  |  j j% |  j t& j' d  |  j)  n  |  j j*   } | t+ j, k re|  j j- t$  d } | t+ j. k r7t j/ } n | t+ j0 k rRt j1 } n  |  j j2 |  n  |  j j3 |  j j4    |  j j5 |  j j6    |  j j7 |  j8  sÍ|  j |  j d |  j8   d S|  j9 |  j  |  j: ró|  j;   n  |  j j<   d S(	   si   
        initializes the configuration. This method is called every time the config file changes
        s   initialize configurations'   No config file found for the given pathNs!   No store found for the given patht   /t   tag_items   handle_cancel()s/   %s: There is no such item recorded in the store(=   R   t   infoR   R   t   CONFIG_PATHR   R   t$   _ReTagController__emit_not_retagableR8   R#   R   t&   _ReTagController__prepare_store_paramst   get_current_languageR9   RB   R   t   get_store_idR*   R,   R.   R0   R>   R:   R;   R<   R=   t   get_expiry_prefixR   t   initR   R   t   get_namet   get_max_tagst   get_tag_seperatort   get_viewt   setModalt   Truet   connectR    t   SIGNALt!   _ReTagController__tag_item_actiont#   _ReTagController__handle_tag_cancelt   get_datestamp_formatR   t   DISABLEDt   show_datestampt   DAYt   DATESTAMP_FORMAT_DAYt   MONTHt   DATESTAMP_FORMAT_MONTHt   set_datestamp_formatt   show_category_linet   get_show_category_linet   set_category_mandatoryt   get_category_mandatoryt   item_existsR"   t/   _ReTagController__set_tag_information_to_dialogR   t#   _ReTagController__handle_retag_modet   show_dialog(   RE   t   format_settingt   format(    (    sB   /home/vk/src/tagstore/research_platform/tagstore/tagstore_retag.pyt   __init_configurationy   s\    
<%(	c         C   s*   |  j  j |  |  j t j d   d  S(   Nt   retag_error(   R   t   errort   emitR    R`   (   RE   t   err_msg(    (    sB   /home/vk/src/tagstore/research_platform/tagstore/tagstore_retag.pyt   __emit_not_retagableÄ   s    c         C   sË   |  j  j |  y |  j  j | | |  Wnd t k
 rb } |  j j |  j d | j     ne t k
 r } |  j j |  j d     n7 X|  j j	 |  |  j
 |  j   |  j t j d   d  S(   Ns7   The Number of free inodes is below the threshold of %s%s   An error occurred while taggingt   retag_success(   R   t   remove_filet   add_item_with_tagsR   R   t   show_messageR8   t   get_thresholdt	   Exceptiont   remove_itemRp   Rx   R    R`   (   RE   t
   store_namet	   file_namet   new_describing_tagst   new_categorizing_tagst   e(    (    sB   /home/vk/src/tagstore/research_platform/tagstore/tagstore_retag.pyt   __handle_retagÈ   s    &c         C   s   | |  _  d S(   s   
        if the manager is called from another qt application (e.g. tagstore.py)
        you must set the calling application here for proper i18n
        N(   R$   (   RE   RF   (    (    sB   /home/vk/src/tagstore/research_platform/tagstore/tagstore_retag.pyt   set_applicationÜ   s    c         C   s6  |  j  j   |  j  j |  j  t j d  |  j  |  j  j |  j  t j d  |  j  d } |  j j	 |  j
  } x6 | D]. } | d k r | } qy d | d | f } qy W|  j  j |  d } |  j j |  j
  } x6 | D]. } | d k rø | } qÝ d | d | f } qÝ W|  j  j |  |  j  j |  j
  t S(   NRP   t    s   %s%s%ss   , (   R   t   set_retag_modet
   disconnectR    R`   Ra   R_   t   _ReTagController__handle_retagR   t   get_describing_tags_for_itemR"   t   set_describing_line_contentt   get_categorizing_tags_for_itemt   set_category_line_contentt   add_pending_itemR^   (   RE   t   cat_contentt   cat_tagst   tagt   desc_contentt	   desc_tags(    (    sB   /home/vk/src/tagstore/research_platform/tagstore/tagstore_retag.pyt   __handle_retag_modeã   s&    %%		c         C   sª  |  j  j | j    |  j j   } t | j |  j j     } | t | j |   B} t | j	 |   } | t | j
 |   B} t |  } | j   râ t | j    } |  j  j t |   t | j |   } n |  j  j | j    t |  | k r| |  } n  |  j  j |  t |  } t |  | k rR| |  } n  |  j  j |  |  j s|  j  j | j   j t j   n  |  j  j | j    d S(   sO   
        convenience method for setting the tag data at the gui-dialog
        N(   R   t   set_tag_listt   get_tagsR   t   get_num_popular_tagst   sett   get_popular_tagsRZ   t   get_recent_tagst   get_popular_categoriest   get_recent_categoriest   listt   is_controlled_vocabularyt   get_controlled_vocabularyt   set_category_listt   intersectiont   get_categorizing_tagst   lent   set_popular_categoriest   set_popular_tagsR   t   set_item_listt   get_pending_changest   get_items_by_eventR   t   ADDEDt   set_store_nameRY   (   RE   t   storet   num_pop_tagst   tag_sett   cat_sett   cat_listt   allowed_sett   tag_list(    (    sB   /home/vk/src/tagstore/research_platform/tagstore/tagstore_retag.pyt   __set_tag_information_to_dialog  s,    	%c         C   sJ  y- |  j  j | | |  |  j j d |  Wnù t k
 rÅ } | j   } | j   } | t j k r |  j	 j
 |  j d |   qF| t j k rµ |  j	 j
 |  j d |   qF|  j d  n t k
 rú } |  j	 j
 |  j d | j     nL t k
 r(} |  j	 j
 |  j d     n X|  j	 j |  |  j	 j   d S(   sI   
        the "tag!" button in the re-tag dialog has been clicked
        s   added item %s to store-filesO   The filename - %s - is in conflict with an already existing tag. Please rename!s;   The tag - %s - is in conflict with an already existing files>   A tag or item is in conflict with an already existing tag/items7   The Number of free inodes is below the threshold of %s%s   An error occurred while taggingN(   R   R}   R   t   debugR   t   get_conflict_typet   get_conflicted_nameR   t   FILER   R~   R8   t   TAGR   R   R   R   t   hide_dialog(   RE   RH   R´   t   category_listR   t   c_typet   c_name(    (    sB   /home/vk/src/tagstore/research_platform/tagstore/tagstore_retag.pyt   __tag_item_action(  s$      &c         C   s   |  j  t j d   d S(   sM   
        the "postpone" button in the re-tag dialog has been clicked
        t   retag_cancelN(   Rx   R    R`   (   RE   (    (    sB   /home/vk/src/tagstore/research_platform/tagstore/tagstore_retag.pyt   __handle_tag_cancelE  s    c         C   s   |  j  j   d  S(   N(   R   Rr   (   RE   (    (    sB   /home/vk/src/tagstore/research_platform/tagstore/tagstore_retag.pyt   show_tag_dialogL  s    c         C   s   |  j  j   d  S(   N(   R   R»   (   RE   (    (    sB   /home/vk/src/tagstore/research_platform/tagstore/tagstore_retag.pyt   hide_tag_dialogO  s    c         C   s   |  j  j |  d S(   sV   
        set the parent for the admin-dialog if there is already a gui window
        N(   t   _ReTagController__admin_dialogt
   set_parent(   RE   t   parent(    (    sB   /home/vk/src/tagstore/research_platform/tagstore/tagstore_retag.pyRÅ   R  s    c         C   s  xu |  j  D]j } |  j j |  j d   |  j j |  j d   |  j j |  j d   |  j j |  j d   q
 W|  j j   } | d k r | |  _	 n  |  j j
   } | d k rÆ | |  _ n  |  j j   } | d k rí | |  _ n  |  j j   } | d k r| |  _ n  d S(   sM   
        initialzes all necessary params for creating a store object
        R   R   t   categorizationR   R   N(   R@   R:   RC   R8   R;   R<   R=   R   t   get_store_config_directoryR*   t   get_store_configfile_nameR,   t   get_store_tagsfile_nameR.   t   get_store_vocabularyfile_nameR0   (   RE   RL   t
   config_dirt   config_file_namet   tags_file_namet   vocabulary_file_name(    (    sB   /home/vk/src/tagstore/research_platform/tagstore/tagstore_retag.pyt   __prepare_store_paramsX  s"    c         C   sw   |  j  j |  j  t j   |  _ t |  } |  j j d | d d  ra |  j  j |  j  n  |  j |  |  _	 d S(   só   
        changes the current application language
        please notice: this method is used to find all available storage/navigation directory names
        this is why it should not be extended to call any UI update methods directly
        R   s   .qms   tsresources/N(
   R$   t   removeTranslatorR5   R    R4   R!   R6   R7   R8   R9   (   RE   RK   t   language(    (    sB   /home/vk/src/tagstore/research_platform/tagstore/tagstore_retag.pyRB   s  s    (   t   __name__t
   __module__t   __doc__R^   R   R   RN   RM   RS   R   R   Rq   Rp   Ra   Rb   RÂ   RÃ   RÅ   RT   RB   (    (    (    sB   /home/vk/src/tagstore/research_platform/tagstore/tagstore_retag.pyR      s    8		K					'						t   ApplicationControllerc           B   s2   e  Z d  Z d   Z d   Z d   Z d   Z RS(   s³   
    a small helper class to launch the retag-dialog as a standalone application
    this helper connects to the signals emitted by the retag controller and does the handling
    c         C   sI  t  j j |   t j |  _ | r1 t j |  _ n  t j |  j  |  _	 t
 t j  |  _ |  j d  k r |  j	 d  |  j   n t j | |  j j    |  _ t j |  |  _ t | |  j |  j t t  |  _ |  j |  j t  j d  |  j  |  j |  j t  j d  |  j  |  j |  j t  j d  |  j  |  j j   d  S(   Ns'   No config file found for the given pathRÀ   Rv   R{   (   R    R   R   R%   R&   R'   R(   R	   RD   t   _ApplicationController__logR   R   RR   t#   _ApplicationController__main_configR   t*   _ApplicationController__handle_retag_errorR
   t   resolve_store_patht   get_store_path_listt"   _ApplicationController__store_patht   get_item_name_from_patht!   _ApplicationController__item_nameR   R^   t   verbose_modet$   _ApplicationController__retag_widgetR_   R`   t+   _ApplicationController__handle_retag_cancelt,   _ApplicationController__handle_retag_successRN   (   RE   RF   t   pathRI   RJ   (    (    sB   /home/vk/src/tagstore/research_platform/tagstore/tagstore_retag.pyR     s     !"""c         C   s   t  j d  d S(   s7   
        exit the program if there is an error
        iÿÿÿÿN(   t   syst   exit(   RE   (    (    sB   /home/vk/src/tagstore/research_platform/tagstore/tagstore_retag.pyt   __handle_retag_error©  s    c         C   s   t  j d  d S(   s1   
        exit the application gracefully
        i    N(   Rä   Rå   (   RE   (    (    sB   /home/vk/src/tagstore/research_platform/tagstore/tagstore_retag.pyt   __handle_retag_success¯  s    c         C   s   t  j d  d S(   s1   
        exit the application gracefully
        i    N(   Rä   Rå   (   RE   (    (    sB   /home/vk/src/tagstore/research_platform/tagstore/tagstore_retag.pyt   __handle_retag_cancelµ  s    (   RÓ   RÔ   RÕ   R   RÙ   Râ   Rá   (    (    (    sB   /home/vk/src/tagstore/research_platform/tagstore/tagstore_retag.pyRÖ     s
   			t   __main__sU   
This program opens a dialog used for tagging an item placed in a tagstore directory.s   tagstore_tag.py -s <item_path>s   -ss   --storet   destRG   t   helps5   absolute  or relative path to the item to be retaggeds   -vs	   --verboseRJ   t   actiont
   store_trues#   start programm with detailed outputs   no store name givent   tagstore_retags   www.tagstore.orgc           C   s   t  j   t j   d  S(   N(   t
   opt_parsert
   print_helpRä   Rå   (    (    (    sB   /home/vk/src/tagstore/research_platform/tagstore/tagstore_retag.pyt   quit_applicationá  s    
(9   Rä   R%   t   PyQt4R    R   t   optparseR   t   tscore.configwrapperR   t   tscore.enumsR   R   R   t   tscore.exceptionsR   R   t   tscore.loghelperR	   t   tscore.pathhelperR
   t   tscore.storeR   t   tscore.tsconstantsR   t   tsgui.tagdialogR   R   R   RÖ   RÓ   t   usageRï   t
   add_optiont
   parse_argst   optionst   argsR   Rß   RI   t   dry_runR   R   RH   RJ   R^   RG   Rð   Rå   t   QApplicationt   argvt   tagstore_tagt   setApplicationNamet   setOrganizationDomaint   UnicodeUTF8t   appcontrollert   exec_Rñ   (    (    (    sB   /home/vk/src/tagstore/research_platform/tagstore/tagstore_retag.pyt   <module>   sN   ÿ i4"			

