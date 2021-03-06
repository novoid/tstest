ó
rPc        	   @   s9  d  d l  Z  d  d l Z d  d l Z d  d l m Z d  d l m Z m Z d  d l	 m
 Z
 d  d l m Z d  d l m Z d  d l m Z m Z m Z d  d l m Z d  d	 l m Z m Z d  d
 l m Z d  d l m Z d  d l m Z d  d l m Z d  d l Z d  d l m  Z  d e j! f d     YZ" e# d k r5e d  Z$ e$ j% d d d d d d d d e$ j% d d d d d d d d e$ j&   \ Z' Z( e) Z* e) Z+ e' j, r½e- Z* n  e' j+ rÏe- Z+ n  e j. e  j/  Z0 e0 j1 d  e0 j2 d  e0 j3 e  j4 d   d! k re  j5   Z6 e7 Z8 e9 e j: d" d#  Z; x e; j<   D] Z= e= Z8 qJWe; j>   e8 e7 k pye8 d$ k r¨e9 e j: d" d%  Z; e; j? e@ e6   n[ e  jA e8  rÚd& e8 GHd' GHd( GHe  jB d)  n) e9 e j: d" d%  Z; e; j? e@ e6   e; j>   n  e" e0 d e* d* e+ ZC e0 jD   n  d S(+   iÿÿÿÿN(   t   OptionParser(   t   QtCoret   QtGui(   t   TagDialogController(   t   ConfigWrapper(   t   Store(   t
   EFileEventt   EDateStampFormatt   EConflictType(   t   TsConstants(   t   NameInConflictExceptiont   InodeShortageException(   t   Administration(   t	   LogHelper(   t   FileSystemWrapper(   t	   PidHelpert   Tagstorec           B   s¡   e  Z d e e d   Z d   Z d   Z d   Z d   Z d   Z	 d   Z
 d   Z d   Z d	   Z d
   Z d   Z d   Z d   Z d   Z d   Z RS(   c         C   s  t  j j |   | |  _ d |  _ | |  _ t t  j j	   j
    d d !|  _ t  j   |  _ |  j j d |  j d d  r |  j j |  j  n  |  j d  |  _ t j |  _ t j |  _ t j |  _ t j |  _ t j |  _ |  j } g  |  _ g  |  _ g  |  _ g  |  _  g  |  _! x |  j D] } |  j" |  |  j! j# |  j d   |  j j# |  j d   |  j j# |  j d	   |  j j# |  j d
   |  j  j# |  j d   q#W|  j" |  t j$ |  _% t j& |  _' t j( |  _) t j* |  _+ t j, |  _- t j. |  _/ g  |  _0 i  |  _1 d |  _2 d |  _3 t4 j5 |  _6 | rQt4 j7 |  _6 n  t8 j9 |  j6  |  _3 |  j3 j: d  |  j;   d S(   sj    
        initializes the configuration. This method is called every time the config file changes
        i    i   t   ts_s   .qms   tsresources/t   ent
   navigationt   storaget   descriptionst
   categoriest   expired_itemss   starting tagstore watcherN(<   R   t   QObjectt   __init__t   _Tagstore__applicationt   Nonet   _Tagstore__admin_widgett   DRY_RUNt   unicodet   QLocalet   systemt   namet   _Tagstore__system_localet   QTranslatort   _Tagstore__translatort   loadt   installTranslatort   trUtf8t   CURRENT_LANGUAGER	   t   DEFAULT_SUPPORTED_LANGUAGESt   SUPPORTED_LANGUAGESt   DEFAULT_STORE_CONFIG_DIRt   STORE_CONFIG_DIRt   DEFAULT_STORE_CONFIG_FILENAMEt   STORE_CONFIG_FILE_NAMEt   DEFAULT_STORE_TAGS_FILENAMEt   STORE_TAGS_FILE_NAMEt!   DEFAULT_STORE_VOCABULARY_FILENAMEt   STORE_VOCABULARY_FILE_NAMEt   STORE_STORAGE_DIRSt   STORE_DESCRIBING_NAV_DIRSt   STORE_CATEGORIZING_NAV_DIRSt   STORE_EXPIRED_DIRSt   STORE_NAVIGATION_DIRSt   change_languaget   appendt   DEFAULT_EXPIRY_PREFIXt   EXPIRY_PREFIXt   DEFAULT_TAG_SEPARATORt   TAG_SEPERATORt   DEFAULT_RECENT_TAGSt   NUM_RECENT_TAGSt   DEFAULT_POPULAR_TAGSt   NUM_POPULAR_TAGSt   DEFAULT_MAX_TAGSt   MAX_TAGSt   DEFAULT_MAX_CLOUD_TAGSt   MAX_CLOUD_TAGSt   STORESt   DIALOGSt   _Tagstore__app_config_wrappert   _Tagstore__logt   loggingt   INFOt	   LOG_LEVELt   DEBUGR   t   get_app_loggert   infot   _Tagstore__init_configurations(   t   selft   applicationt   parentt   verboset   dryrunt   store_current_languaget   lang(    (    s   tagstore.pyR   )   sX    			% 										c         C   s  |  j  j d  t t j  |  _ |  j j |  j t j d  |  j	  |  j j
   |  j j   } | j   d k r | |  _ n  |  j j   } | j   d k r® | |  _ n  |  j j   |  _ |  j j   |  _ |  j j   |  _ |  j j   |  _ |  j d k s|  j d k r)|  j d  |  _ n  |  j |  j  |  j j   } | d k r`| |  _ n  |  j j   } | d k r| |  _ n  |  j j   } | d k r®| |  _ n  |  j j    } | d k rÕ| |  _! n  |  j j"   } |  j j#   } g  }	 x¢ |  j$ D] }
 |
 j%   } | | k r|
 j& |  j j' |  |  j d |  j |  j d |  j |  j d |  j!  |
 j( |  j  | j) |  q|	 j* |
  qWx7 |	 D]/ }
 |  j$ j) |
  |  j  j+ d |
 j,    q¥Wx¨| D] } | d | k rßt- | d | d |  j d |  j |  j d |  j |  j d |  j! |  j. |  j/ |  j0 |  j1 |  j2 |  j  }
 |
 j |
 t j d	  |  j3  |
 j |
 t j d
  |  j4  |
 j |
 t j d  |  j5  |
 j |
 t j d  |  j6  |
 j |
 t j d  |  j7  |
 j |
 t j d  |  j8  |
 j |
 t j d  |  j9  |  j j:   } t; |  d k r}| d d k r}|
 j< |  n  |  j$ j* |
  |  j  j+ d |
 j,    t= |
 j,   |
 j%   |  j |  j |  j  } | j | t j d  |  j>  | j | t j d  |  j?  | j | t j d  |  j@  | |  jA |
 j%   <|
 jB   |  j | t j d  |  jC  |  jD |
 |  qßqßWd S(   si   
        initializes the configuration. This method is called every time the config file changes
        s   initialize configurations	   changed()t    R   t   /s   removed store: %st   idt   paths   removed(PyQt_PyObject)s   renamed(PyQt_PyObject, QString)s-   file_renamed(PyQt_PyObject, QString, QString)s$   file_removed(PyQt_PyObject, QString)s)   pending_operations_changed(PyQt_PyObject)t   vocabulary_changedt   store_config_changedi    s   init store: %st   tag_items   handle_cancel()s   open_store_admin_dialog()t   item_selectedN(E   RI   RO   R   R	   t   CONFIG_PATHRH   t   connectR   t   SIGNALRP   t   print_app_config_to_logt   get_tag_seperatort   stripR=   t   get_expiry_prefixR;   t   get_num_popular_tagsR?   RA   t   get_max_tagsRC   t   get_current_languageR(   R   R'   R8   t   get_store_config_directoryR,   t   get_store_configfile_nameR.   t   get_store_tagsfile_nameR0   t   get_store_vocabularyfile_nameR2   t
   get_storest   get_store_idsRF   t   get_idt   set_patht   get_store_patht   change_expiry_prefixt   removeR9   t   debugt   get_nameR   R7   R3   R4   R5   R6   t   store_removedt   store_renamedt   file_renamedt   file_removedt   pending_file_operationst$   _Tagstore__handle_vocabulary_changedt&   _Tagstore__handle_store_config_changedt    get_additional_ignored_extensiont   lent   add_ignored_extensionsR   t   tag_item_actiont   handle_cancelt   show_admin_dialogRG   t   initt0   _Tagstore__set_tag_information_to_dialog_wrappert   _Tagstore__configure_tag_dialog(   RQ   t   tag_seperatort   expiry_prefixt
   config_dirt   config_file_namet   tags_file_namet   vocabulary_file_namet   config_store_itemst   config_store_idst   deleted_storest   storeRZ   t
   store_itemt
   extensionst
   tmp_dialog(    (    s   tagstore.pyt   __init_configurationsm   s    %"-
c         C   s­   | j    } | j   } | t j k r | j t  d } | t j k rU t j	 } n | t j
 k rp t j } n  | j | |  n  | j | j    | j | j    d S(   sV   
        given a store and a tag dialog - promote all settings to the dialog 
        N(   t   get_datestamp_formatt   get_datestamp_hiddenR   t   DISABLEDt   show_datestampt   TrueR   t   DAYR	   t   DATESTAMP_FORMAT_DAYt   MONTHt   DATESTAMP_FORMAT_MONTHt   set_datestamp_formatt   show_category_linet   get_show_category_linet   set_category_mandatoryt   get_category_mandatory(   RQ   R   R   t   format_settingt	   is_hiddent   format(    (    s   tagstore.pyt   __configure_tag_dialogæ   s    c         C   s   |  j  |  d  S(   N(   t(   _Tagstore__set_tag_information_to_dialog(   RQ   R   (    (    s   tagstore.pyt   __handle_vocabulary_changedü   s    c         C   s'   |  j  | j   } |  j | |  d  S(   N(   RG   Rp   R   (   RQ   R   t   dialog_controller(    (    s   tagstore.pyt   __handle_store_config_changedÿ   s    c         C   sX   t  |  j d t |  _ |  j j t  |  j j |  j   j    |  j j	 t  d  S(   NRT   (
   R   R   t   verbose_modeR   t	   set_modalR   t
   set_parentt   sendert   get_viewR   (   RQ   (    (    s   tagstore.pyR     s    c         C   s   |  j  j | j    d S(   s:   
        event handler of the stores remove event
        N(   RH   t   remove_storeRp   (   RQ   R   (    (    s   tagstore.pyRw     s    c         C   s   |  j  j | j   |  d S(   s:   
        event handler of the stores rename event
        N(   RH   t   rename_storeRp   (   RQ   R   t   new_path(    (    s   tagstore.pyRx     s    c         C   s.   |  j  j d | | f  | j | |  d S(   s1   
        event handler for: file renamed
        s   ..........file renamed %s, %sN(   RI   Ru   t   rename_file(   RQ   R   t   old_file_namet   new_file_name(    (    s   tagstore.pyRy     s    c         C   s%   |  j  j d |  | j |  d S(   s1   
        event handler for: file renamed
        s   ...........file removed %sN(   RI   Ru   t   remove_file(   RQ   R   t	   file_name(    (    s   tagstore.pyRz      s    c         C   s  |  j  j d  |  j | j   } | j   t | j   j t j	   } t | j   j t j
   } | | B} | d k s t |  d k r | j   d S|  j  j d | j   | j   j   f  x | D] } | j |  qÒ W|  j |  | j   d S(   sM   
        event handler: handles all operations with user interaction
        s    new pending file operation addedi    Ns   store: %s, item: %s (   RI   RO   RG   Rp   t   clear_tagdialogt   sett   get_pending_changest   get_items_by_eventR   t   ADDEDt   ADDED_OR_RENAMEDR   R   t   hide_dialogRu   t	   to_stringt   add_pending_itemR§   t   show_dialog(   RQ   R   R©   t
   added_listt   added_renamed_listt
   whole_listt   item(    (    s   tagstore.pyR{   '  s    


,c         C   s:   |  j    } | d  k s( t | t  r, d  S| j   d  S(   N(   R®   R   t
   isinstanceR   R¾   (   RQ   R©   (    (    s   tagstore.pyR   E  s    c         C   s:   x3 |  j  D]( } | j   | k r
 |  j |  q
 q
 Wd  S(   N(   RF   Rp   R§   (   RQ   t   store_idR   (    (    s   tagstore.pyt'   __set_tag_information_to_dialog_wrapperK  s    c   
   
   C   s9  |  j  j d  |  j | j   } | j | j    | j   } | d k	 r5t |  d k r5| d d k	 r5| j	   d k s¦ | j	   d k s¦ | j	   d k r| j
 |  j t | d j     } g  } | j   r8t | j    } | j t |   xL | D]( } | t |  k r	| j |  q	q	Wn | j | j    | } | j t | d j     } | j | | |  j  n  | j	   d k s¿| j	   d k s¿| j	   d k r| j |  j t | d j     }	 | j t | d j     } | j | |	 |  j  n  | j | j    q5n  d S(   sR   
        convenience method for refreshing the tag data at the gui-dialog
        s!   refresh tag information on dialogi    i   i   i   N(   RI   Ru   RG   Rp   t   set_tag_listt   get_tagst   get_selected_item_list_publicR   R   t   get_tagline_configt   get_cat_recommendationRA   t   strt   textt   is_controlled_vocabularyR¹   t   get_controlled_vocabularyt   set_category_listt   listR9   t   get_categorizing_tagst   get_cat_cloudt   set_cat_cloudRE   t   get_tag_recommendationt   get_tag_cloudt   set_tag_cloudt   set_store_nameRv   (
   RQ   R   R©   t	   item_listt   tmp_cat_listt   cat_listt   allowed_sett   catt   dictt   tag_list(    (    s   tagstore.pyt   __set_tag_information_to_dialogP  s0    "6%6%c         C   s  d } x- |  j D]" } | | j   k r | } Pq q W| d k	 r|  j | j   } y* | j | | |  |  j j d |  Wní t k
 r} | j	   }	 | j
   }
 |	 t j k rÕ | j |  j d |
   q|	 t j k r| j |  j d |
   q|  j d  qt k
 rC} | j |  j d | j     qt k
 rn} | j |  j d     qX| j |  |  j |  n  d S(   s@   
        write the tags for the given item to the store
        s   added items %s to store-filesO   The filename - %s - is in conflict with an already existing tag. Please rename!s;   The tag - %s - is in conflict with an already existing files>   A tag or item is in conflict with an already existing tag/items7   The Number of free inodes is below the threshold of %s%s   An error occurred while taggingN(   R   RF   Rv   RG   Rp   t   add_item_list_with_tagsRI   Ru   R
   t   get_conflict_typet   get_conflicted_nameR   t   FILEt   show_messageR'   t   TAGR   t   get_thresholdt	   Exceptiont   remove_item_listR§   (   RQ   t
   store_namet   item_name_listRá   t   category_listR   t
   loop_storeR©   t   et   c_typet   c_name(    (    s   tagstore.pyR     s2    #c         C   sw   |  j  j |  j  t j   |  _ t |  } |  j j d | d d  ra |  j  j |  j  n  |  j |  |  _	 d S(   só   
        changes the current application language
        please notice: this method is used to find all available storage/navigation directory names
        this is why it should not be extended to call any UI update methods directly
        R   s   .qms   tsresources/N(
   R   t   removeTranslatorR$   R   R#   R   R%   R&   R'   R(   (   RQ   t   localet   language(    (    s   tagstore.pyR8   ®  s    N(   t   __name__t
   __module__R   t   FalseR   RP   R   R|   R}   R   Rw   Rx   Ry   Rz   R{   R   R   R§   R   R8   (    (    (    s   tagstore.pyR   '   s    D	y												5	)t   __main__s   tagstore.py [options]s   -vs	   --verboset   destRT   t   actiont
   store_truet   helps"   start program with detailed outputs   -ds	   --dry-runt   dry_runsL   test-mode. actions are just written to ouput. no changes to filesystem made.t   tagstores   www.tagstore.orgi   t   wint   PID_FILEt   rRX   t   wsS   EXIT - A tagstore process is already running with PID %s. So this time I just quit.sp   Please note that tagstore is only watching for new files in the stores. If you want to modify tagstore settings,s   please start tagstore_manageriþÿÿÿRU   (E   t   syst   ost   logging.handlersRJ   t   optparseR    t   PyQt4R   R   t   tsgui.tagdialogR   t   tscore.configwrapperR   t   tscore.storeR   t   tscore.enumsR   R   R   t   tscore.tsconstantsR	   t   tscore.exceptionsR
   R   t   tagstore_managerR   t   tscore.loghelperR   t   tsos.filesystemR   t   errnot   tscore.pidhelperR   R   R   Rö   t
   opt_parsert
   add_optiont
   parse_argst   optionst   argsRø   R«   Rþ   RT   R   t   QApplicationt   argvRÿ   t   setApplicationNamet   setOrganizationDomaint   UnicodeUTF8t   platformt   get_current_pidt   pidR   t   old_pidt   opent
   CONFIG_DIRt   pid_filet	   readlinest   linet   closet   writeRÎ   t
   pid_existst   exitt
   tag_widgett   exec_(    (    (    s   tagstore.pyt   <module>   sj   ÿ ""				

	