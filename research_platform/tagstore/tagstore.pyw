ó
Ä9Nc        	   @   sg  d  d l  Z  d  d l Z d  d l Z d  d l m Z d  d l m Z m Z d  d l	 m
 Z
 d  d l m Z d  d l m Z d  d l m Z m Z m Z d  d l m Z d  d	 l m Z m Z d  d
 l m Z d  d l m Z d  d l m Z d  d l m Z d  d l Z d e j f d     YZ  e! d k rce d  Z" e" j# d d d d d d d d e" j# d d d d d d d d e" j$   \ Z% Z& e' Z( e' Z) e% j* r­e+ Z( n  e% j) r¿e+ Z) n  e j, e  j-  Z. e. j/ d  e. j0 d  e. j1 e  j2 d  d  k r>e j3   Z4 e5 Z6 e7 e j8 d! d"  Z9 x e9 j:   D] Z; e; Z6 q:We9 j<   e6 e5 k pie6 d# k re7 e j8 d! d$  Z9 e9 j= e> e4   n y: e j? e@ e6  d%  d& e6 GHd' GHd( GHe  jA d)  Wn\ eB k
 r0ZC e jD GHeC j e jD k r*e7 e j8 d! d$  Z9 e9 j= e> e4   n   n Xe9 j<   n  e  e. d e( d* e) ZE e. jF   n  d S(+   iÿÿÿÿN(   t   OptionParser(   t   QtCoret   QtGui(   t   TagDialogController(   t   ConfigWrapper(   t   Store(   t
   EFileEventt   EDateStampFormatt   EConflictType(   t   TsConstants(   t   NameInConflictExceptiont   InodeShortageException(   t   Administration(   t	   LogHelper(   t   FileSystemWrappert   Tagstorec           B   s   e  Z d e e d   Z d   Z d   Z d   Z d   Z d   Z	 d   Z
 d   Z d   Z d	   Z d
   Z d   Z d   Z d   Z d   Z RS(   c         C   so  t  j j |   | |  _ | |  _ t t  j j   j    d d !|  _	 t  j
   |  _ |  j j d |  j	 d d  r |  j j |  j  n  |  j d  |  _ t j |  _ t j |  _ t j |  _ t j |  _ t j |  _ |  j } g  |  _ g  |  _ g  |  _ g  |  _ g  |  _ x |  j D] } |  j  |  |  j j! |  j d   |  j j! |  j d   |  j j! |  j d	   |  j j! |  j d
   |  j j! |  j d   qW|  j  |  t j" |  _# t j$ |  _% t j& |  _' t j( |  _) t j* |  _+ g  |  _, i  |  _- d |  _/ d |  _0 t1 j2 |  _3 | r<t1 j4 |  _3 n  t5 j6 |  j3  |  _0 |  j0 j7 d  |  j8   d S(   sj    
        initializes the configuration. This method is called every time the config file changes
        i    i   t   ts_s   .qms   tsresources/t   ent
   navigationt   storaget   descriptionst
   categoriest   expired_itemss   starting tagstore watcherN(9   R   t   QObjectt   __init__t   _Tagstore__applicationt   DRY_RUNt   unicodet   QLocalet   systemt   namet   _Tagstore__system_localet   QTranslatort   _Tagstore__translatort   loadt   installTranslatort   trUtf8t   CURRENT_LANGUAGER	   t   DEFAULT_SUPPORTED_LANGUAGESt   SUPPORTED_LANGUAGESt   DEFAULT_STORE_CONFIG_DIRt   STORE_CONFIG_DIRt   DEFAULT_STORE_CONFIG_FILENAMEt   STORE_CONFIG_FILE_NAMEt   DEFAULT_STORE_TAGS_FILENAMEt   STORE_TAGS_FILE_NAMEt!   DEFAULT_STORE_VOCABULARY_FILENAMEt   STORE_VOCABULARY_FILE_NAMEt   STORE_STORAGE_DIRSt   STORE_DESCRIBING_NAV_DIRSt   STORE_CATEGORIZING_NAV_DIRSt   STORE_EXPIRED_DIRSt   STORE_NAVIGATION_DIRSt   change_languaget   appendt   DEFAULT_EXPIRY_PREFIXt   EXPIRY_PREFIXt   DEFAULT_TAG_SEPARATORt   TAG_SEPERATORt   DEFAULT_RECENT_TAGSt   NUM_RECENT_TAGSt   DEFAULT_POPULAR_TAGSt   NUM_POPULAR_TAGSt   DEFAULT_MAX_TAGSt   MAX_TAGSt   STORESt   DIALOGSt   Nonet   _Tagstore__app_config_wrappert   _Tagstore__logt   loggingt   INFOt	   LOG_LEVELt   DEBUGR   t   get_app_loggert   infot   _Tagstore__init_configurations(   t   selft   applicationt   parentt   verboset   dryrunt   store_current_languaget   lang(    (    s   ./tagstore.pyR   '   sT    		% 										c         C   s  |  j  j d  t t j  |  _ |  j j |  j t j d  |  j	  |  j j
   |  j j   } | j   d k r | |  _ n  |  j j   } | j   d k r® | |  _ n  |  j j   |  _ |  j j   |  _ |  j j   |  _ |  j j   |  _ |  j d k s|  j d k r)|  j d  |  _ n  |  j |  j  |  j j   } | d k r`| |  _ n  |  j j   } | d k r| |  _ n  |  j j   } | d k r®| |  _ n  |  j j    } | d k rÕ| |  _! n  |  j j"   } |  j j#   } g  }	 x¢ |  j$ D] }
 |
 j%   } | | k r|
 j& |  j j' |  |  j d |  j |  j d |  j |  j d |  j!  |
 j( |  j  | j) |  q|	 j* |
  qWx7 |	 D]/ }
 |  j$ j) |
  |  j  j+ d |
 j,    q¥Wx?| D]7} | d | k rßt- | d | d |  j d |  j |  j d |  j |  j d |  j! |  j. |  j/ |  j0 |  j1 |  j2 |  j  }
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
 t j d  |  j9  |  j$ j* |
  |  j  j+ d |
 j,    t: |
 j,   |  j |  j |  j  } | j | t j d  |  j;  | j | t j d  |  j<  | j | t j d  |  j=  |  j> |
 |  | |  j? |
 j%   <|
 j@   qßqßWd S(   si   
        initializes the configuration. This method is called every time the config file changes
        s   initialize configurations	   changed()t    R   t   /s   removed store: %st   idt   paths   removed(PyQt_PyObject)s   renamed(PyQt_PyObject, QString)s-   file_renamed(PyQt_PyObject, QString, QString)s$   file_removed(PyQt_PyObject, QString)s)   pending_operations_changed(PyQt_PyObject)t   vocabulary_changedt   store_config_changeds   init store: %st   tag_items   handle_cancel()s   open_store_admin_dialog()N(A   RE   RK   R   R	   t   CONFIG_PATHRD   t   connectR   t   SIGNALRL   t   print_app_config_to_logt   get_tag_seperatort   stripR:   t   get_expiry_prefixR8   t   get_num_popular_tagsR<   R>   t   get_max_tagsR@   t   get_current_languageR%   RC   R$   R5   t   get_store_config_directoryR)   t   get_store_configfile_nameR+   t   get_store_tagsfile_nameR-   t   get_store_vocabularyfile_nameR/   t
   get_storest   get_store_idsRA   t   get_idt   set_patht   get_store_patht   change_expiry_prefixt   removeR6   t   debugt   get_nameR   R4   R0   R1   R2   R3   t   store_removedt   store_renamedt   file_renamedt   file_removedt   pending_file_operationst$   _Tagstore__handle_vocabulary_changedt&   _Tagstore__handle_store_config_changedR   t   tag_item_actiont   handle_cancelt   show_admin_dialogt   _Tagstore__configure_tag_dialogRB   t   init(   RM   t   tag_seperatort   expiry_prefixt
   config_dirt   config_file_namet   tags_file_namet   vocabulary_file_namet   config_store_itemst   config_store_idst   deleted_storest   storeRV   t
   store_itemt
   tmp_dialog(    (    s   ./tagstore.pyt   __init_configurationsh   s    %$c         C   s­   | j    } | j   } | t j k r | j t  d } | t j k rU t j	 } n | t j
 k rp t j } n  | j | |  n  | j | j    | j | j    d S(   sV   
        given a store and a tag dialog - promote all settings to the dialog 
        N(   t   get_datestamp_formatt   get_datestamp_hiddenR   t   DISABLEDt   show_datestampt   TrueRC   t   DAYR	   t   DATESTAMP_FORMAT_DAYt   MONTHt   DATESTAMP_FORMAT_MONTHt   set_datestamp_formatt   show_category_linet   get_show_category_linet   set_category_mandatoryt   get_category_mandatory(   RM   R   R   t   format_settingt	   is_hiddent   format(    (    s   ./tagstore.pyt   __configure_tag_dialogÚ   s    c         C   s   |  j  |  d  S(   N(   t(   _Tagstore__set_tag_information_to_dialog(   RM   R   (    (    s   ./tagstore.pyt   __handle_vocabulary_changedð   s    c         C   s'   |  j  | j   } |  j | |  d  S(   N(   RB   Rk   R|   (   RM   R   t   dialog_controller(    (    s   ./tagstore.pyt   __handle_store_config_changedó   s    c         C   sI   t  t d t } | j t  | j |  j   j    | j t  d  S(   NRP   (	   R   t   tagstoret   verbose_modet	   set_modalR   t
   set_parentt   sendert   get_viewR{   (   RM   t   admin_widget(    (    s   ./tagstore.pyR{   ø   s    c         C   s   |  j  j | j    d S(   s:   
        event handler of the stores remove event
        N(   RD   t   remove_storeRk   (   RM   R   (    (    s   ./tagstore.pyRr   ÿ   s    c         C   s   |  j  j | j   |  d S(   s:   
        event handler of the stores rename event
        N(   RD   t   rename_storeRk   (   RM   R   t   new_path(    (    s   ./tagstore.pyRs     s    c         C   s.   |  j  j d | | f  | j | |  d S(   s1   
        event handler for: file renamed
        s   ..........file renamed %s, %sN(   RE   Rp   t   rename_file(   RM   R   t   old_file_namet   new_file_name(    (    s   ./tagstore.pyRt     s    c         C   s%   |  j  j d |  | j |  d S(   s1   
        event handler for: file renamed
        s   ...........file removed %sN(   RE   Rp   t   remove_file(   RM   R   t	   file_name(    (    s   ./tagstore.pyRu     s    c         C   s  |  j  j d  |  j | j   } | j   t | j   j t j	   } t | j   j t j
   } | | B} | d k s t |  d k r | j   d S|  j  j d | j   | j   j   f  x | D] } | j |  qÒ W|  j |  | j   d S(   sM   
        event handler: handles all operations with user interaction
        s    new pending file operation addedi    Ns   store: %s, item: %s (   RE   RK   RB   Rk   t   clear_tagdialogt   sett   get_pending_changest   get_items_by_eventR   t   ADDEDt   ADDED_OR_RENAMEDRC   t   lent   hide_dialogRp   t	   to_stringt   add_pending_itemR   t   show_dialog(   RM   R   R   t
   added_listt   added_renamed_listt
   whole_listt   item(    (    s   ./tagstore.pyRv     s    


,c         C   s:   |  j    } | d  k s( t | t  r, d  S| j   d  S(   N(   R¥   RC   t
   isinstanceR   R·   (   RM   R   (    (    s   ./tagstore.pyRz   9  s    c         C   s  |  j  j d  |  j | j   } | j | j    t | j |  j   } | t | j	 |  j
   B} t | j |  j   } | t | j |  j
   B} t |  } | j   ró t | j    } | j t |   t | j |   } n | j | j    t |  |  j k r+| |  j  } n  | j |  t |  } t |  |  j k ri| |  j  } n  | j |  | j | j    d S(   sR   
        convenience method for refreshing the tag data at the gui-dialog
        s!   refresh tag information on dialogN(   RE   Rp   RB   Rk   t   set_tag_listt   get_tagsR±   t   get_popular_tagsR>   t   get_recent_tagsR<   t   get_popular_categoriest   get_recent_categoriest   listt   is_controlled_vocabularyt   get_controlled_vocabularyt   set_category_listt   intersectiont   get_categorizing_tagsR¶   t   set_popular_categoriest   set_popular_tagst   set_store_nameRq   (   RM   R   R   t   tag_sett   cat_sett   cat_listt   allowed_sett   tag_list(    (    s   ./tagstore.pyt   __set_tag_information_to_dialog?  s*    c         C   s  d } x- |  j D]" } | | j   k r | } Pq q W|  j | j   } y* | j | | |  |  j j d |  Wní t k
 r} | j	   }	 | j
   }
 |	 t j k rÉ | j |  j d |
   q}|	 t j k rõ | j |  j d |
   q}|  j d  nx t k
 r7} | j |  j d | j     nF t k
 rb} | j |  j d     n X| j |  |  j |  d S(   s@   
        write the tags for the given item to the store
        s   added items %s to store-filesO   The filename - %s - is in conflict with an already existing tag. Please rename!s;   The tag - %s - is in conflict with an already existing files>   A tag or item is in conflict with an already existing tag/items7   The Number of free inodes is below the threshold of %s%s   An error occurred while taggingN(   RC   RA   Rq   RB   Rk   t   add_item_list_with_tagsRE   Rp   R
   t   get_conflict_typet   get_conflicted_nameR   t   FILEt   show_messageR$   t   TAGR   t   get_thresholdt	   Exceptiont   remove_item_listR   (   RM   t
   store_namet   item_name_listRÓ   t   category_listR   t
   loop_storeR   t   et   c_typet   c_name(    (    s   ./tagstore.pyRy   e  s0    #c         C   sq   t  j |  j  t j   |  _ t |  } |  j j d | d d  r[ t  j |  j  n  |  j |  |  _	 d S(   só   
        changes the current application language
        please notice: this method is used to find all available storage/navigation directory names
        this is why it should not be extended to call any UI update methods directly
        R   s   .qms   tsresources/N(
   R¡   t   removeTranslatorR!   R   R    R   R"   R#   R$   R%   (   RM   t   localet   language(    (    s   ./tagstore.pyR5     s    N(   t   __name__t
   __module__RC   t   FalseR   RL   R|   Rw   Rx   R{   Rr   Rs   Rt   Ru   Rv   Rz   R   Ry   R5   (    (    (    s   ./tagstore.pyR   %   s   A	r											&	't   __main__s   tagstore.py [options]s   -vs	   --verboset   destRP   t   actiont
   store_truet   helps"   start program with detailed outputs   -ds	   --dry-runt   dry_runsL   test-mode. actions are just written to ouput. no changes to filesystem made.R¡   s   www.tagstore.orgi   t   wint   PID_FILEt   rRT   t   wi    sS   EXIT - A tagstore process is already running with PID %s. So this time I just quit.sp   Please note that tagstore is only watching for new files in the stores. If you want to modify tagstore settings,s   please start tagstore_manageriþÿÿÿRQ   (G   t   syst   ost   logging.handlersRF   t   optparseR    t   PyQt4R   R   t   tsgui.tagdialogR   t   tscore.configwrapperR   t   tscore.storeR   t   tscore.enumsR   R   R   t   tscore.tsconstantsR	   t   tscore.exceptionsR
   R   t   tagstore_managerR   t   tscore.loghelperR   t   tsos.filesystemR   t   errnoR   R   Rè   t
   opt_parsert
   add_optiont
   parse_argst   optionst   argsRê   R¢   Rð   RP   R   t   QApplicationt   argvR¡   t   setApplicationNamet   setOrganizationDomaint   UnicodeUTF8t   platformt   getpidt   pidRC   t   old_pidt   opent
   CONFIG_DIRt   pid_filet	   readlinest   linet   closet   writet   strt   killt   intt   exitt   OSErrorRâ   t   ESRCHt
   tag_widgett   exec_(    (    (    s   ./tagstore.pyt   <module>   sr   ÿ |""				

	