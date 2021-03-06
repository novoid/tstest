ó
"HPc        	   @   sÀ  d  d l  m Z m Z d  d l m Z d  d l m Z d  d l m Z d  d l	 m
 Z
 m Z d  d l m Z m Z d  d l m Z d  d l m Z d  d	 l m Z d  d
 l m Z d  d l m Z d  d l Z d  d l Z d e j f d     YZ e d k r¼e d  Z e j  d d d d d d d d e j!   \ Z" Z# e$ Z% e$ Z& e" j' r]e( Z% n  e j) e j*  Z+ e+ j, d  e+ j- d  e+ j. e e+ d e% Z/ e/ j0 e(  e+ j1   n  d S(   iÿÿÿÿ(   t   QtCoret   QtGui(   t   OptionParser(   t   ReTagController(   t   ConfigWrapper(   t   EDateStampFormatt   EConflictType(   t   NameInConflictExceptiont   InodeShortageException(   t	   LogHelper(   t   Store(   t   TsConstants(   t   StorePreferencesController(   t   TagDialogControllerNt   Administrationc           B   sû   e  Z d    Z d   Z d   Z d   Z d   Z d   Z d   Z d   Z	 d   Z
 d	   Z d
   Z d   Z d   Z d   Z d   Z d   Z d   Z d   Z d   Z d   Z d   Z d   Z d   Z d   Z d   Z d   Z d   Z RS(   c         C   s  t  j j |   d  |  _ d  |  _ d  |  _ d  |  _ | |  _ | |  _	 t
 j |  _ | rg t
 j |  _ n  t j |  _ t j |  _ t j |  _ t j |  _ t t  j j   j    d d !|  _ t  j   |  _ |  j j d |  j d d  r|  j	 j |  j  n  |  j    |  _! g  |  _" g  |  _# g  |  _$ g  |  _% g  |  _& t j' |  _( i  |  _) x |  j( D] } |  j* |  |  j" j+ |  j, d   |  j# j+ |  j, d   |  j$ j+ |  j, d   |  j% j+ |  j, d	   |  j& j+ |  j, d
   q\Wt- j. |  j  |  _ |  j/   d  S(   Ni    i   t   ts_s   .qms   tsresources/t   storaget   descriptionst
   categoriest   expired_itemst
   navigation(0   R    t   QObjectt   __init__t   Nonet   _Administration__logt   _Administration__main_configt   _Administration__admin_dialogt   _Administration__retag_dialogt   _Administration__verbose_modet   _Administration__applicationt   loggingt   INFOt	   LOG_LEVELt   DEBUGR   t   DEFAULT_STORE_CONFIG_DIRt   STORE_CONFIG_DIRt   DEFAULT_STORE_CONFIG_FILENAMEt   STORE_CONFIG_FILE_NAMEt   DEFAULT_STORE_TAGS_FILENAMEt   STORE_TAGS_FILE_NAMEt!   DEFAULT_STORE_VOCABULARY_FILENAMEt   STORE_VOCABULARY_FILE_NAMEt   unicodet   QLocalet   systemt   namet   _Administration__system_localet   QTranslatort   _Administration__translatort   loadt   installTranslatort$   _Administration__get_locale_languaget   CURRENT_LANGUAGEt   STORE_STORAGE_DIRSt   STORE_DESCRIBING_NAV_DIRSt   STORE_CATEGORIZING_NAV_DIRSt   STORE_EXPIRED_DIRSt   STORE_NAVIGATION_DIRSt   DEFAULT_SUPPORTED_LANGUAGESt   SUPPORTED_LANGUAGESt   _Administration__store_dictt   change_languaget   appendt   trUtf8R	   t   get_app_loggert#   _Administration__init_configuration(   t   selft   applicationt   verboset   lang(    (    s   tagstore_manager.pyR   "   sF    						% 						c         C   s  |  j  j d  |  j d k r4 t t j  |  _ n  |  j j   |  _ |  j d k sd |  j d k rv |  j	   |  _ n  |  j
 |  j  |  j d k r´t   |  _ |  j |  j t j d  |  j  |  j |  j t j d  |  j  |  j |  j t j d  |  j  |  j |  j t j d  |  j  |  j |  j t j d  |  j  |  j |  j t j d  |  j  |  j |  j t j d	  |  j  |  j |  j t j d
  |  j  n  |  j j |  j  |  j   |  j   g  } xk |  j j   D]Z } | d j d  j   } |  j | j   | d <|  j | j   | d <| j  |  qñW|  j j! |  |  j j"   r|  j j# t$  n  d S(   si   
        initializes the configuration. This method is called every time the config file changes
        s   initialize configurationt    t   create_new_storet   rename_desc_tagt   rename_cat_tagt   retagt   rebuild_storet   rename_storet   delete_storet   synchronizet   patht   /t	   desc_tagst   cat_tagsN(%   R   t   infoR   R   R   R   t   CONFIG_PATHt   get_current_languageR4   R3   R=   R   R   t   connectR    t   SIGNALt!   _Administration__handle_new_storet"   _Administration__handle_tag_renamet!   _Administration__handle_retaggingt%   _Administration__handle_store_rebuildt$   _Administration__handle_store_renamet$   _Administration__handle_store_deletet'   _Administration__handle_synchronizationt   set_main_configt%   _Administration__prepare_store_paramst   _Administration__create_storest
   get_storest   splitt   popR<   t   get_tagst   get_categorizing_tagsR>   t   set_store_listt   get_first_startt   set_first_startt   True(   RB   t   tmp_store_listt   current_store_itemt
   store_name(    (    s   tagstore_manager.pyt   __init_configurationR   s:    """""""%

c         C   sF   |  j  t |  } d GHd | GHd GH| j d d g d d g  d S(   sE   
        do all the necessary synchronization stuff here ...
        s   ####################s   synchronize t   item_onet   item_twot   bet   toughN(   R<   t   strt   add_item_list_with_tags(   RB   Rm   t   store_to_sync(    (    s   tagstore_manager.pyt   __handle_synchronization   s
    	c         C   s   |  j  j |  j d   |  j t |  } | |  _ |  j | t j d  |  j	  | j
   |  j | t j d  |  j  |  j j | j    d  S(   Ns   Deleting store ...t   store_delete_end(   R   t   start_progressbarR?   R<   Rs   t$   _Administration__store_to_be_deletedRV   R    RW   t%   _Administration__handle_store_deletedt   removet
   disconnectt   _Administration__dummyR   t   remove_storet   get_id(   RB   Rm   t   store(    (    s   tagstore_manager.pyt   __handle_store_delete   s    	
c         C   s   d S(   Nt   dummy(    (   RB   (    (    s   tagstore_manager.pyt   __dummy   s    c         C   s   |  j  j |  j  d  S(   N(   R   t   remove_store_itemRy   (   RB   t   id(    (    s   tagstore_manager.pyt   __handle_store_deleted   s    c         C   s   |  j  j |  j d   |  j j t |   } |  j j | j   |  |  j	 | t
 j d  |  j  | j |  |  j | t
 j d   |  j   d S(   s   
        the whole store directory gets moved to the new directory
        the store will be rebuilt then to make sure all links are updated
        s   Moving store ...t   store_rebuild_endN(   R   Rx   R?   R<   Rd   Rs   R   RL   R   RV   R    RW   R[   t   moveR|   RA   (   RB   Rm   t   new_store_nameR   (    (    s   tagstore_manager.pyt   __handle_store_rename¡   s    c         C   sx   |  j  j |  j d   |  j t |  } |  j | t j d  |  j  | j	   |  j
 | t j d  |  j  d S(   sf   
        the whole store structure will be rebuild according to the records in store.tgs file
        s   Rebuilding store ...R   N(   R   Rx   R?   R<   Rs   RV   R    RW   R[   t   rebuildR|   R3   (   RB   Rm   R   (    (    s   tagstore_manager.pyt   __handle_store_rebuildµ   s
    
c         C   s   |  j  j   d  S(   N(   R   t   stop_progressbar(   RB   Rm   (    (    s   tagstore_manager.pyt   __hide_progress_dialogÀ   s    c         C   s   |  j  d  S(   sH   
        returns the translation of "en" in the system language
        t   en(   R?   (   RB   (    (    s   tagstore_manager.pyt   __get_locale_languageÄ   s    c   	      C   sc   |  j  | } | j   } t |  } | j   } t |  } | j t | d  t | d   d  S(   Ns   utf-8(   R<   t   toUtf8Rs   t
   rename_tagR*   (	   RB   t   old_tagt   new_tagRm   R   t   old_bat   old_strt   new_bat   new_str(    (    s   tagstore_manager.pyt   __handle_tag_renameÊ   s    c         C   s   | |  _  d S(   s   
        if the manager is called from another qt application (e.g. tagstore.py)
        you must set the calling application here for proper i18n
        N(   R   (   RB   RC   (    (    s   tagstore_manager.pyt   set_applicationÔ   s    c         C   sÑ   |  j  | } | j   } |  j j d | | f  t |  j | j   | t |  j  |  _	 |  j
 |  j	 t j d  |  j  |  j
 |  j	 t j d  |  j  |  j
 |  j	 t j d  |  j  |  j	 j   d S(   sT   
        creates and configures a tag-dialog with all store-params and tags
        s    retagging item %s at store %s...t   retag_errort   retag_cancelt   retag_successN(   R<   t   textR   RS   R   R   t   get_store_pathRj   R   R   RV   R    RW   t#   _Administration__handle_retag_errort$   _Administration__handle_retag_cancelt%   _Administration__handle_retag_successt   start(   RB   Rm   t	   item_nameR   (    (    s   tagstore_manager.pyt   __handle_retaggingÛ   s    '"""c         C   s   |  j  j   d |  _  d S(   s4   
        hide the dialog and set it to None
        N(   R   t   hide_tag_dialogR   (   RB   (    (    s   tagstore_manager.pyt   __kill_tag_dialogî   s    c         C   s'   |  j    |  j j |  j d   d  S(   Ns"   An error occurred while re-tagging(   t    _Administration__kill_tag_dialogR   t   show_tooltipR?   (   RB   (    (    s   tagstore_manager.pyt   __handle_retag_errorõ   s    
c         C   s'   |  j    |  j j |  j d   d  S(   Ns   Re-tagging successful!(   R¨   R   R©   R?   (   RB   (    (    s   tagstore_manager.pyt   __handle_retag_successù   s    
c         C   s   |  j    d S(   sM   
        the "postpone" button in the re-tag dialog has been clicked
        N(   R¨   (   RB   (    (    s   tagstore_manager.pyt   __handle_retag_cancelý   s    c         C   s|  |  j  j | j    |  j j   } t | j |  j j     } | t | j |   B} t | j	 |   } | t | j
 |   B} t |  } | j   râ t | j    } |  j  j t |   t | j |   } n |  j  j | j    t |  | k r| |  } n  |  j  j |  t |  } t |  | k rR| |  } n  |  j  j |  |  j  j | j    d S(   sO   
        convenience method for setting the tag data at the gui-dialog
        N(   R   t   set_tag_listRe   R   t   get_num_popular_tagst   sett   get_popular_tagst   get_max_tagst   get_recent_tagst   get_popular_categoriest   get_recent_categoriest   listt   is_controlled_vocabularyt   get_controlled_vocabularyt   set_category_listt   intersectionRf   t   lent   set_popular_categoriest   set_popular_tagst   set_store_namet   get_name(   RB   R   t   num_pop_tagst   tag_sett   cat_sett   cat_listt   allowed_sett   tag_list(    (    s   tagstore_manager.pyt   __set_tag_information_to_dialog  s(    c   	      C   sT  |  j  | } y* | j | | |  |  j j d |  Wnù t k
 rÏ } | j   } | j   } | t j k r |  j	 j
 |  j d |   qP| t j k r¿ |  j	 j
 |  j d |   qP|  j d  n t k
 r} |  j	 j
 |  j d | j     nL t k
 r2} |  j	 j
 |  j d     n X|  j	 j |  |  j	 j   d S(   sI   
        the "tag!" button in the re-tag dialog has been clicked
        s   added item %s to store-filesO   The filename - %s - is in conflict with an already existing tag. Please rename!s;   The tag - %s - is in conflict with an already existing files>   A tag or item is in conflict with an already existing tag/items7   The Number of free inodes is below the threshold of %s%s   An error occurred while taggingN(   R<   t   add_item_with_tagsR   t   debugR   t   get_conflict_typet   get_conflicted_nameR   t   FILER   t   show_messageR?   t   TAGR   t   get_thresholdt	   Exceptiont   remove_itemt   hide_dialog(	   RB   Rm   R¤   RÄ   t   category_listR   t   et   c_typet   c_name(    (    s   tagstore_manager.pyt   __retag_item_action(  s&      &c         C   s   |  j  j   d  S(   N(   R   t   show_dialog(   RB   t   show(    (    s   tagstore_manager.pyt   show_admin_dialogF  s    c         C   s   |  j  j |  d S(   sV   
        set the parent for the admin-dialog if there is already a gui window
        N(   R   t
   set_parent(   RB   t   parent(    (    s   tagstore_manager.pyRÙ   I  s    c         C   s   |  j  j |  d S(   sC   
        True- if the admin dialog should be in modal-mode
        N(   R   t	   set_modal(   RB   t   modal(    (    s   tagstore_manager.pyRÛ   O  s    c         C   s  xu |  j  D]j } |  j j |  j d   |  j j |  j d   |  j j |  j d   |  j j |  j d   q
 W|  j j   } | d k r | |  _	 n  |  j j
   } | d k rÆ | |  _ n  |  j j   } | d k rí | |  _ n  |  j j   } | d k r| |  _ n  d S(   sM   
        initialzes all necessary params for creating a store object
        R   R   t   categorizationR   RF   N(   R;   R5   R>   R?   R6   R7   R8   R   t   get_store_config_directoryR#   t   get_store_configfile_nameR%   t   get_store_tagsfile_nameR'   t   get_store_vocabularyfile_nameR)   (   RB   RE   t
   config_dirt   config_file_namet   tags_file_namet   vocabulary_file_name(    (    s   tagstore_manager.pyt   __prepare_store_paramsU  s"    c         C   sw   |  j  j |  j  t j   |  _ t |  } |  j j d | d d  ra |  j  j |  j  n  |  j |  |  _	 d S(   só   
        changes the current application language
        please notice: this method is used to find all available storage/navigation directory names
        this is why it should not be extended to call any UI update methods directly
        R   s   .qms   tsresources/N(
   R   t   removeTranslatorR0   R    R/   R*   R1   R2   R?   R4   (   RB   t   localet   language(    (    s   tagstore_manager.pyR=   p  s    c         C   s%  |  j  j   } x| D]} | d j d  j   } t | d | d |  j d |  j |  j d |  j |  j d |  j |  j	 |  j
 |  j |  j |  j |  j  j    } | j   | |  j | <|  j | t j d  |  j  |  j | t j d  |  j  |  j | t j d  |  j  q Wd  S(   NRO   RP   R   R   Rw   t   store_rename_end(   R   Rb   Rc   Rd   R
   R#   R%   R'   R)   R9   R5   R6   R7   R8   t   get_expiry_prefixt   initR<   RV   R    RW   t%   _Administration__hide_progress_dialog(   RB   t   store_itemsRl   Rm   t	   tmp_store(    (    s   tagstore_manager.pyt   __create_stores  s$    
c         C   s&   |  j  j |  } |  j | |  d S(   s5   
        create new store at given directory
        N(   R   t   add_new_storet(   _Administration__create_new_store_object(   RB   t   dirt   store_id(    (    s   tagstore_manager.pyt   __handle_new_store  s    c         C   s   t  | | |  j d |  j |  j d |  j |  j d |  j |  j |  j |  j |  j |  j	 |  j
 j    } | j   |  j   d  S(   NRP   (   R
   R#   R%   R'   R)   R9   R5   R6   R7   R8   R   Rë   Rì   RA   (   RB   Rô   RO   Rï   (    (    s   tagstore_manager.pyt   __create_new_store_object¢  s    	
(   t   __name__t
   __module__R   RA   R^   R]   R}   Rz   R\   R[   Rí   R3   RY   R   RZ   R¨   R    R¢   R¡   t.   _Administration__set_tag_information_to_dialogt"   _Administration__retag_item_actionRØ   RÙ   RÛ   R`   R=   Ra   RX   Rò   (    (    (    s   tagstore_manager.pyR       s6   	0	2									
							%								t   __main__s   tagstore_manager.py [options]s   -vs	   --verboset   destRD   t   actiont
   store_truet   helps#   start programm with detailed outputt   tagstore_managers   www.tagstore.org(2   t   PyQt4R    R   t   optparseR   t   tagstore_retagR   t   tscore.configwrapperR   t   tscore.enumsR   R   t   tscore.exceptionsR   R   t   tscore.loghelperR	   t   tscore.storeR
   t   tscore.tsconstantsR   t   tsgui.admindialogR   t   tsgui.tagdialogR   t   logging.handlersR   t   sysR   R   R÷   t
   opt_parsert
   add_optiont
   parse_argst   optionst   argst   Falset   verbose_modet   dry_runRD   Rj   t   QApplicationt   argvt   tagstore_admint   setApplicationNamet   setOrganizationDomaint   UnicodeUTF8t   admin_widgetRØ   t   exec_(    (    (    s   tagstore_manager.pyt   <module>   s:   ÿ "		