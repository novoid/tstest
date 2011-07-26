ó
|ÚMc        	   @   sÀ  d  d l  Z  d  d l Z d  d l m Z d  d l m Z m Z d  d l m	 Z	 d  d l
 m Z d  d l m Z d  d l m Z d  d l m Z d  d	 l m Z d  d
 l m Z m Z d  d l m Z m Z d  d l m Z d e j f d     YZ e d k r¼e d  Z e j  d d d d d d d d e j!   \ Z" Z# e$ Z% e$ Z& e" j' r]e( Z% n  e j) e  j*  Z+ e+ j, d  e+ j- d  e+ j. e e+ d e% Z/ e/ j0 e(  e+ j1   n  d S(   iÿÿÿÿN(   t   OptionParser(   t   QtCoret   QtGui(   t   Store(   t   ConfigWrapper(   t   TsConstants(   t   StorePreferencesController(   t	   LogHelper(   t   TagDialogController(   t   EDateStampFormatt   EConflictType(   t   NameInConflictExceptiont   InodeShortageException(   t   ReTagControllert   Administrationc           B   s³   e  Z d    Z d   Z d   Z d   Z d   Z d   Z d   Z d   Z	 d   Z
 d	   Z d
   Z d   Z d   Z d   Z d   Z d   Z d   Z d   Z d   Z RS(   c         C   s
  t  j j |   d  |  _ d  |  _ d  |  _ d  |  _ | |  _ t	 j
 |  _ | r^ t	 j |  _ n  t j |  _ t j |  _ t j |  _ t j |  _ t t  j j   j    d d !|  _ t  j   |  _ |  j j d |  j d d  rø |  j j |  j  n  |  j   |  _  g  |  _! g  |  _" g  |  _# g  |  _$ g  |  _% t j& |  _' i  |  _( x |  j' D] } |  j) |  |  j! j* |  j+ d   |  j" j* |  j+ d   |  j# j* |  j+ d   |  j$ j* |  j+ d	   |  j% j* |  j+ d
   qSWt, j- |  j  |  _ |  j.   d  S(   Ni    i   t   ts_s   .qms   tsresources/t   storaget   descriptionst
   categoriest   expired_itemst
   navigation(/   R   t   QObjectt   __init__t   Nonet   _Administration__logt   _Administration__main_configt   _Administration__admin_dialogt   _Administration__retag_dialogt   _Administration__applicationt   loggingt   INFOt	   LOG_LEVELt   DEBUGR   t   DEFAULT_STORE_CONFIG_DIRt   STORE_CONFIG_DIRt   DEFAULT_STORE_CONFIG_FILENAMEt   STORE_CONFIG_FILE_NAMEt   DEFAULT_STORE_TAGS_FILENAMEt   STORE_TAGS_FILE_NAMEt!   DEFAULT_STORE_VOCABULARY_FILENAMEt   STORE_VOCABULARY_FILE_NAMEt   unicodet   QLocalet   systemt   namet   _Administration__system_localet   QTranslatort   _Administration__translatort   loadt   installTranslatort$   _Administration__get_locale_languaget   CURRENT_LANGUAGEt   STORE_STORAGE_DIRSt   STORE_DESCRIBING_NAV_DIRSt   STORE_CATEGORIZING_NAV_DIRSt   STORE_EXPIRED_DIRSt   STORE_NAVIGATION_DIRSt   DEFAULT_SUPPORTED_LANGUAGESt   SUPPORTED_LANGUAGESt   _Administration__store_dictt   change_languaget   appendt   trUtf8R   t   get_app_loggert#   _Administration__init_configuration(   t   selft   applicationt   verboset   lang(    (    sD   /home/vk/src/tagstore/research_platform/tagstore/tagstore_manager.pyR   "   sD    					% 						c         C   së  |  j  j d  t t j  |  _ |  j j   |  _ |  j d k sR |  j d k rd |  j	   |  _ n  |  j
 |  j  |  j d k rt   |  _ |  j |  j t j d  |  j  |  j |  j t j d  |  j  |  j |  j t j d  |  j  |  j |  j t j d  |  j  n  |  j j |  j  |  j   |  j   g  } xk |  j j   D]Z } | d j d  j   } |  j | j   | d	 <|  j | j   | d
 <| j |  qWW|  j j |  |  j j   rç|  j j t   n  d S(   si   
        initializes the configuration. This method is called every time the config file changes
        s   initialize configurationt    t   create_new_storet   rename_desc_tagt   rename_cat_tagt   retagt   patht   /t	   desc_tagst   cat_tagsN(!   R   t   infoR   R   t   CONFIG_PATHR   t   get_current_languageR3   R   R2   R<   R   R   t   connectR   t   SIGNALt!   _Administration__handle_new_storet"   _Administration__handle_tag_renamet!   _Administration__handle_retaggingt   set_main_configt%   _Administration__prepare_store_paramst   _Administration__create_storest
   get_storest   splitt   popR;   t   get_tagst   get_categorizing_tagsR=   t   set_store_listt   get_first_startt   set_first_startt   True(   RA   t   tmp_store_listt   current_store_itemt
   store_name(    (    sD   /home/vk/src/tagstore/research_platform/tagstore/tagstore_manager.pyt   __init_configurationQ   s0    """%

c         C   s   |  j  d  S(   sH   
        returns the translation of "en" in the system language
        t   en(   R>   (   RA   (    (    sD   /home/vk/src/tagstore/research_platform/tagstore/tagstore_manager.pyt   __get_locale_languagey   s    c   	      C   sc   |  j  | } | j   } t |  } | j   } t |  } | j t | d  t | d   d  S(   Ns   utf-8(   R;   t   toUtf8t   strt
   rename_tagR)   (	   RA   t   old_tagt   new_tagRd   t   storet   old_bat   old_strt   new_bat   new_str(    (    sD   /home/vk/src/tagstore/research_platform/tagstore/tagstore_manager.pyt   __handle_tag_rename   s    c         C   s   | |  _  d S(   s   
        if the manager is called from another qt application (e.g. tagstore.py)
        you must set the calling application here for proper i18n
        N(   R   (   RA   RB   (    (    sD   /home/vk/src/tagstore/research_platform/tagstore/tagstore_manager.pyt   set_application   s    c         C   sÎ   |  j  | } | j   } |  j j d | | f  t |  j | j   | t t  |  _	 |  j
 |  j	 t j d  |  j  |  j
 |  j	 t j d  |  j  |  j
 |  j	 t j d  |  j  |  j	 j   d S(   sT   
        creates and configures a tag-dialog with all store-params and tags
        s    retagging item %s at store %s...t   retag_errort   retag_cancelt   retag_successN(   R;   t   textR   RN   R   R   t   get_store_pathRa   t   verbose_modeR   RQ   R   RR   t#   _Administration__handle_retag_errort$   _Administration__handle_retag_cancelt%   _Administration__handle_retag_successt   start(   RA   Rd   t	   item_nameRm   (    (    sD   /home/vk/src/tagstore/research_platform/tagstore/tagstore_manager.pyt   __handle_retagging   s    $"""c         C   s   |  j  j   d |  _  d S(   s4   
        hide the dialog and set it to None
        N(   R   t   hide_tag_dialogR   (   RA   (    (    sD   /home/vk/src/tagstore/research_platform/tagstore/tagstore_manager.pyt   __kill_tag_dialog£   s    c         C   s'   |  j    |  j j |  j d   d  S(   Ns"   An error occurred while re-tagging(   t    _Administration__kill_tag_dialogR   t   show_tooltipR>   (   RA   (    (    sD   /home/vk/src/tagstore/research_platform/tagstore/tagstore_manager.pyt   __handle_retag_errorª   s    
c         C   s'   |  j    |  j j |  j d   d  S(   Ns   Re-tagging successful!(   R   R   R   R>   (   RA   (    (    sD   /home/vk/src/tagstore/research_platform/tagstore/tagstore_manager.pyt   __handle_retag_success®   s    
c         C   s   |  j    d S(   sM   
        the "postpone" button in the re-tag dialog has been clicked
        N(   R   (   RA   (    (    sD   /home/vk/src/tagstore/research_platform/tagstore/tagstore_manager.pyt   __handle_retag_cancel²   s    c         C   s|  |  j  j | j    |  j j   } t | j |  j j     } | t | j |   B} t | j	 |   } | t | j
 |   B} t |  } | j   râ t | j    } |  j  j t |   t | j |   } n |  j  j | j    t |  | k r| |  } n  |  j  j |  t |  } t |  | k rR| |  } n  |  j  j |  |  j  j | j    d S(   sO   
        convenience method for setting the tag data at the gui-dialog
        N(   R   t   set_tag_listR\   R   t   get_num_popular_tagst   sett   get_popular_tagst   get_max_tagst   get_recent_tagst   get_popular_categoriest   get_recent_categoriest   listt   is_controlled_vocabularyt   get_controlled_vocabularyt   set_category_listt   intersectionR]   t   lent   set_popular_categoriest   set_popular_tagst   set_store_namet   get_name(   RA   Rm   t   num_pop_tagst   tag_sett   cat_sett   cat_listt   allowed_sett   tag_list(    (    sD   /home/vk/src/tagstore/research_platform/tagstore/tagstore_manager.pyt   __set_tag_information_to_dialog¸   s(    c   	      C   sT  |  j  | } y* | j | | |  |  j j d |  Wnù t k
 rÏ } | j   } | j   } | t j k r |  j	 j
 |  j d |   qP| t j k r¿ |  j	 j
 |  j d |   qP|  j d  n t k
 r} |  j	 j
 |  j d | j     nL t k
 r2} |  j	 j
 |  j d     n X|  j	 j |  |  j	 j   d S(   sI   
        the "tag!" button in the re-tag dialog has been clicked
        s   added item %s to store-filesO   The filename - %s - is in conflict with an already existing tag. Please rename!s;   The tag - %s - is in conflict with an already existing files>   A tag or item is in conflict with an already existing tag/items7   The Number of free inodes is below the threshold of %s%s   An error occurred while taggingN(   R;   t   add_item_with_tagsR   t   debugR   t   get_conflict_typet   get_conflicted_nameR
   t   FILER   t   show_messageR>   t   TAGR   t   get_thresholdt	   Exceptiont   remove_itemt   hide_dialog(	   RA   Rd   R~   R   t   category_listRm   t   et   c_typet   c_name(    (    sD   /home/vk/src/tagstore/research_platform/tagstore/tagstore_manager.pyt   __retag_item_actionÝ   s&      &c         C   s   |  j  j   d  S(   N(   R   t   show_dialog(   RA   t   show(    (    sD   /home/vk/src/tagstore/research_platform/tagstore/tagstore_manager.pyt   show_admin_dialogû   s    c         C   s   |  j  j |  d S(   sV   
        set the parent for the admin-dialog if there is already a gui window
        N(   R   t
   set_parent(   RA   t   parent(    (    sD   /home/vk/src/tagstore/research_platform/tagstore/tagstore_manager.pyR³   þ   s    c         C   s   |  j  j |  d S(   sC   
        True- if the admin dialog should be in modal-mode
        N(   R   t	   set_modal(   RA   t   modal(    (    sD   /home/vk/src/tagstore/research_platform/tagstore/tagstore_manager.pyRµ     s    c         C   s  xu |  j  D]j } |  j j |  j d   |  j j |  j d   |  j j |  j d   |  j j |  j d   q
 W|  j j   } | d k r | |  _	 n  |  j j
   } | d k rÆ | |  _ n  |  j j   } | d k rí | |  _ n  |  j j   } | d k r| |  _ n  d S(   sM   
        initialzes all necessary params for creating a store object
        R   R   t   categorizationR   RE   N(   R:   R4   R=   R>   R5   R6   R7   R   t   get_store_config_directoryR"   t   get_store_configfile_nameR$   t   get_store_tagsfile_nameR&   t   get_store_vocabularyfile_nameR(   (   RA   RD   t
   config_dirt   config_file_namet   tags_file_namet   vocabulary_file_name(    (    sD   /home/vk/src/tagstore/research_platform/tagstore/tagstore_manager.pyt   __prepare_store_params
  s"    c         C   sw   |  j  j |  j  t j   |  _ t |  } |  j j d | d d  ra |  j  j |  j  n  |  j |  |  _	 d S(   só   
        changes the current application language
        please notice: this method is used to find all available storage/navigation directory names
        this is why it should not be extended to call any UI update methods directly
        R   s   .qms   tsresources/N(
   R   t   removeTranslatorR/   R   R.   R)   R0   R1   R>   R3   (   RA   t   localet   language(    (    sD   /home/vk/src/tagstore/research_platform/tagstore/tagstore_manager.pyR<   %  s    c         C   sÈ   |  j  j   } x² | D]ª } | d j d  j   } t | d | d |  j d |  j |  j d |  j |  j d |  j |  j	 |  j
 |  j |  j |  j |  j  j    } | j   | |  j | <q Wd  S(   NRJ   RK   t   id(   R   RY   RZ   R[   R   R"   R$   R&   R(   R8   R4   R5   R6   R7   t   get_expiry_prefixt   initR;   (   RA   t   store_itemsRc   Rd   t	   tmp_store(    (    sD   /home/vk/src/tagstore/research_platform/tagstore/tagstore_manager.pyt   __create_stores9  s    
c         C   s   |  j  j |  } t | | |  j d |  j |  j d |  j |  j d |  j |  j |  j |  j	 |  j
 |  j |  j  j    } | j   |  j   d S(   s5   
        create new store at given directory
        RK   N(   R   t   add_new_storeR   R"   R$   R&   R(   R8   R4   R5   R6   R7   RÅ   RÆ   R@   (   RA   t   dirt   store_idRÈ   (    (    sD   /home/vk/src/tagstore/research_platform/tagstore/tagstore_manager.pyt   __handle_new_storeL  s    	
(   t   __name__t
   __module__R   R@   R2   RT   Rs   RU   R   Rz   R|   R{   t.   _Administration__set_tag_information_to_dialogt"   _Administration__retag_item_actionR²   R³   Rµ   RW   R<   RX   RS   (    (    (    sD   /home/vk/src/tagstore/research_platform/tagstore/tagstore_manager.pyR       s&   	/	(		
							%							t   __main__s   tagstore_manager.py [options]s   -vs	   --verboset   destRC   t   actiont
   store_truet   helps#   start programm with detailed outputt   tagstore_managers   www.tagstore.org(2   t   syst   logging.handlersR   t   optparseR    t   PyQt4R   R   t   tscore.storeR   t   tscore.configwrapperR   t   tscore.tsconstantsR   t   tsgui.admindialogR   t   tscore.loghelperR   t   tsgui.tagdialogR   t   tscore.enumsR	   R
   t   tscore.exceptionsR   R   t   tagstore_retagR   R   R   RÎ   t
   opt_parsert
   add_optiont
   parse_argst   optionst   argst   FalseRy   t   dry_runRC   Ra   t   QApplicationt   argvt   tagstore_admint   setApplicationNamet   setOrganizationDomaint   UnicodeUTF8t   admin_widgetR²   t   exec_(    (    (    sD   /home/vk/src/tagstore/research_platform/tagstore/tagstore_manager.pyt   <module>   s:   ÿ B"		