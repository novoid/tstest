�
~e�Pc        	   @   s�  d  d l  Z  d  d l Z d  d l Z d  d l Z d  d l Z d  d l Z d  d l Z d  d l m Z m	 Z	 d  d l
 m Z d  d l m Z d  d l m Z d  d l m Z d  d l m Z d  d l m Z d	 e j f d
 �  �  YZ d e j f d �  �  YZ e d k r�d Z e d � Z e j d d d d d d �e j d d d d d d �e j d d d d d d d d �e j d  d! d d" d d d d# �e j �  \ Z Z e  Z! e  Z" e# Z$ e# Z% e j& r�e' Z% n  e j( r�e j! Z! n  e j) r�e j) Z" n  e j$ re' Z$ n  e! e  k o#e" e  k r`e! e" k r]d$ GHe! GHe" GHe$ GHe j* �  e  j+ �  n  n  e	 j, e  j- � Z. e. j/ d% � e. j0 d& � e. j1 e e. e! e" e$ e% � Z2 e. j3 �  n  d' �  Z4 d S((   i����N(   t   QtCoret   QtGui(   t   OptionParser(   t	   LogHelper(   t   ConfigWrapper(   t   TsConstants(   t   Store(   t   SyncDialogControllert   SyncControllerc           B   sL  e  Z e d  � Z d �  Z d �  Z d �  Z d �  Z d �  Z d �  Z	 d �  Z
 d �  Z d	 �  Z d
 �  Z d �  Z d �  Z d �  Z e d � Z d �  Z d �  Z d �  Z d �  Z d �  Z d �  Z e d � Z d �  Z d �  Z d �  Z d �  Z d �  Z d �  Z d �  Z  d �  Z! d �  Z" d �  Z# d  �  Z$ d! �  Z% d" �  Z& RS(#   c         C   sa  t  j j |  � | |  _ | |  _ | |  _ | |  _ d |  _ d |  _	 d |  _
 d |  _ d |  _ d |  _ d |  _ d |  _ d |  _ t j |  _ t j |  _ t j |  _ t j |  _ t j |  _ t t  j j �  j �  � d d !} t  j  �  |  _! |  j! j" d | d d � r%|  j j# |  j! � n  |  j$ d � |  _% g  |  _& g  |  _' g  |  _( g  |  _) g  |  _* t j+ |  _, i  |  _- x� |  j, D]� } |  j. | � |  j* j/ |  j$ d � � |  j& j/ |  j$ d � � |  j' j/ |  j$ d	 � � |  j( j/ |  j$ d
 � � |  j) j/ |  j$ d � � q�W|  j. |  j% � t0 j1 |  _2 | rHt0 j3 |  _2 n  t4 j5 |  j2 � |  _6 d S(   s+   
        initialize the controller
        i    i   t   ts_s   .qms   tsresources/t   ent
   navigationt   storaget   descriptionst
   categoriest   expired_itemsN(7   R    t   QObjectt   __init__t   _SyncController__applicationt"   _SyncController__source_store_patht"   _SyncController__target_store_patht   _SyncController__auto_synct   Nonet   _SyncController__main_configt   _SyncController__store_configt   _SyncController__source_storet   _SyncController__target_storet   _SyncController__sync_dialogt#   _SyncController__conflict_file_listt   _SyncController__source_itemst   _SyncController__target_itemst"   _SyncController__target_sync_itemsR   t   DEFAULT_STORE_CONFIG_DIRt   STORE_CONFIG_DIRt   DEFAULT_STORE_CONFIG_FILENAMEt   STORE_CONFIG_FILE_NAMEt   DEFAULT_STORE_TAGS_FILENAMEt   STORE_TAGS_FILE_NAMEt!   DEFAULT_STORE_VOCABULARY_FILENAMEt   STORE_VOCABULARY_FILE_NAMEt    DEFAULT_STORE_SYNC_TAGS_FILENAMEt   STORE_SYNC_FILE_NAMEt   unicodet   QLocalet   systemt   namet   QTranslatort   _SyncController__translatort   loadt   installTranslatort   trUtf8t   CURRENT_LANGUAGEt   STORE_STORAGE_DIRSt   STORE_DESCRIBING_NAV_DIRSt   STORE_CATEGORIZING_NAV_DIRSt   STORE_EXPIRED_DIRSt   STORE_NAVIGATION_DIRSt   DEFAULT_SUPPORTED_LANGUAGESt   SUPPORTED_LANGUAGESt   _SyncController__store_dictt   change_languaget   appendt   loggingt   INFOt	   LOG_LEVELt   DEBUGR   t   get_app_loggert   _SyncController__log(   t   selft   applicationt   source_store_patht   target_store_patht	   auto_synct   verboset   localet   lang(    (    s   tagstore_sync.pyR   &   sV    													"						c         C   s   |  j  �  d S(   s<   
        call this method to launch the sync dialog
        N(   t#   _SyncController__init_configuration(   RD   (    (    s   tagstore_sync.pyt   startg   s    c      
   C   s
  |  j  j d � t t j � |  _ |  j d k rK |  j |  j d � � d St	 } t	 } |  j
 d k r~ |  j
 d k r~ t } n  t	 } t	 } |  j d k r� |  j d k r� t } n  g  } |  j j �  } |  j j �  } | d k r| d k ri  } | | d <d | d <| j | � n  d }	 x� | D]� }
 |
 j d � rF|
 d }	 n |
 d j d � j �  }	 |
 d } |	 |
 d <| |
 d <| j |
 � | r�| |  j
 k r�t } q�n  | r$| |  j k r�t } q�q$q$W| r�| t	 k r�|  j |  j d	 � � d S| r(| t	 k r(|  j |  j d
 � � d S|  j d k r�t | |  j
 |  j |  j � |  _ |  j j �  j t � |  j j |  j t j d � |  j � |  j j |  j t j d � |  j � |  j j |  j t j d � |  j � n  |  j j �  |  j r|  j j �  n  d S(   s/   
        initializes the configuration
        t   __init_configurations'   No config file found for the given pathNt    t   patht   AndroidR-   t   /s-   Source tagstore not registered in main configs-   Target tagstore not registered in main configt
   sync_storet   sync_conflicts   handle_cancel()(    RC   t   infoR   R   t   CONFIG_PATHR   R   t"   _SyncController__emit_not_syncableR2   t   FalseR   t   TrueR   t
   get_storest   get_android_store_pathR=   t   has_keyt   splitt   popR   R   R   t   get_viewt   setModalt   connectR    t   SIGNALt"   _SyncController__sync_store_actiont(   _SyncController__handle_resolve_conflictt#   _SyncController__handle_sync_cancelt   show_dialogt   start_auto_sync(   RD   t   search_source_patht   found_source_patht   search_target_patht   found_target_patht   tmp_store_listt
   store_listt   android_source_patht
   store_itemt
   store_namet   current_store_itemt
   store_path(    (    s   tagstore_sync.pyRN   m   sf    		




!%%(	c         C   s�   |  j  j | � | d } | d } | d } | d } |  j | | � } |  j j d | | | f � | d k r� |  j | | | | | � n  |  j �  d  S(   Nt   source_itemt   target_itemt   source_storet   target_storesA   handle_resolve_conclict: source_item %s target_item %s action % st   replace(   R   t   removet(   _SyncController__create_target_file_pathRC   RU   t#   _SyncController__sync_conflict_filet%   _SyncController__show_conflict_dialog(   RD   t	   file_itemt   actionRs   Rt   Ru   Rv   t   target_file_path(    (    s   tagstore_sync.pyt   __handle_resolve_conflict�   s    



c         C   s   |  j  j �  |  j j �  d S(   s>   
        removes the lock from the affected tagstores
        N(   R   t   remove_sync_lock_fileR   (   RD   (    (    s   tagstore_sync.pyt   __remove_lock_file�   s    c         C   sd   |  j  j �  s |  j j �  r" t S|  j  j �  } | s; | S|  j j �  } | s` |  j  j �  n  | S(   sC   
        creates the lock files for the affected tagstores
        (   R   t   is_sync_activeR   RX   t   create_sync_lock_fileR�   (   RD   t   result(    (    s   tagstore_sync.pyt   __create_lock_file�   s    c         C   se  x� t  |  j � d k r� |  j d } | d } | d } | d } | d } | d } | d } |  j | | | | | t � } | r� |  j j | � q n  d | }	 |  j j |	 � d	 |  j | | � |  j | | � f }	 |  j j d
 |	 | � d SWd t	 j	 j
 �  j d � }
 |  j �  |  j j |
 � |  j j t � |  j j |  j d � � |  j �  d S(   sT   
        displays the conflict dialogs when there are one or more conflicts
        i    Rs   Rt   Ru   Rv   t   target_itemst   target_sync_itemss
   Syncing %ss&   Do you want to replace file %s with %st   ConflictNs   Sync completed on s   %Y-%m-%d %H:%M:%St   Finish(   t   lenR   t   _SyncController__sync_itemRX   Rx   R   t   set_status_msgt#   _SyncController__get_full_file_patht   show_conflict_dialogt   datetimet   utcnowt   strftimet   _SyncController__flush_changest   toggle_sync_buttonRY   t   set_close_button_textR2   t!   _SyncController__remove_lock_file(   RD   t   current_itemRs   Rt   Ru   Rv   R�   R�   t   sync_successt   messaget   msg(    (    s   tagstore_sync.pyt   __show_conflict_dialog�   s.    






(
c         C   s�   t  | � |  _ |  j d k r8 |  j |  j d � � d St |  j j �  | |  j d |  j |  j d |  j	 |  j d |  j
 |  j |  j |  j |  j |  j |  j j �  � |  _ |  j j �  d S(   s0   
        create the source store object
        s(   No source store found for the given pathNRR   (   R   R   R   RW   R2   R   t   get_store_idR!   R#   R%   R'   R8   R4   R5   R6   R7   R   t   get_expiry_prefixR   t   init(   RD   Ru   (    (    s   tagstore_sync.pyt   __create_source_store*  s    c         C   sB   |  j  d  k r |  j  j �  n  |  j d  k r> |  j j �  n  d  S(   N(   R   R   t   finish_syncR   (   RD   (    (    s   tagstore_sync.pyt   __flush_changesC  s    c         C   s�   t  | � |  _ |  j d k r8 |  j |  j d � � d St |  j j �  | |  j d |  j |  j d |  j	 |  j d |  j
 |  j |  j |  j |  j |  j |  j j �  � |  _ |  j j �  d S(   s0   
        create the target store object
        s(   No target store found for the given pathNRR   (   R   t$   _SyncController__target_store_configR   RW   R2   R   R�   R!   R#   R%   R'   R8   R4   R5   R6   R7   R   R�   R   R�   (   RD   Rv   (    (    s   tagstore_sync.pyt   __create_target_storeK  s    c         C   sd   |  j  j �  } |  j j �  } g  } x9 | D]1 } |  j |  j  | | � r+ | j | � q+ q+ q+ W| S(   sF   
        returns all files which have the associated sync tag
        (   R   t	   get_itemsR   t   get_sync_tagt   _SyncController__has_sync_tagR=   (   RD   t   source_itemst   sync_tagt   source_sync_itemsRs   (    (    s   tagstore_sync.pyt   __get_file_items_with_sync_tagc  s    
c         C   s�   |  j  | | � |  j j �  p+ |  j j �  } | rF |  j �  |  _ n |  j j �  |  _ |  j j �  |  _ |  j j �  |  _	 d S(   s#   
        prepares the sync
        N(
   t   _SyncController__init_storesR   t   is_android_storeR   t-   _SyncController__get_file_items_with_sync_tagR   R�   R   t   get_sync_itemsR   (   RD   Ru   Rv   t   android_sync(    (    s   tagstore_sync.pyt   __prepare_sync}  s    c         C   s�   g  |  _  |  j | | � |  j �  } | sh |  j j d � |  j j d � |  j j |  j d � � d S|  j	 �  |  j | | � |  j	 �  |  j j d t
 |  j  � � |  j �  d S(   s&   
        initializes the sync
        s<   another sync is in progress please wait until it is finisheds9   Another sync is pending, please wait until it is finishedR�   Ns   Number of conflicts %d(   R   t   _SyncController__prepare_synct!   _SyncController__create_lock_fileRC   RU   R   R�   R�   R2   t   _SyncController__handle_syncR�   R{   (   RD   Ru   Rv   t	   lock_file(    (    s   tagstore_sync.pyt   __sync_store_action�  s    	

c         C   s,   |  j  |  j |  j |  j |  j |  j � d S(   s!   
        executes a sync
        N(   t   _SyncController__start_syncR   R   R   R   R   (   RD   (    (    s   tagstore_sync.pyt   __handle_sync�  s    c         C   s]   |  j  | | � } | d  k r= |  j | | | | | | | � S|  j | | | | | | � Sd  S(   N(   t#   _SyncController__find_item_in_storeR   t#   _SyncController__sync_existing_itemt   _SyncController__sync_new_item(   RD   Ru   Rv   R�   R�   Rs   t   add_conflict_listRt   (    (    s   tagstore_sync.pyt   __sync_item�  s    c         C   s#  |  j  | | � } | r0 |  j j d | � t S|  j | | � } t j j | � s� |  j j d | � |  j | | | | � t S|  j	 | | � }	 |  j
 | | | |	 � }
 |
 r� |  j j d | � |  j | | | | d t �t S|  j j d | � | r|  j | | | | | |	 � n  t S(   Ns(   [SKIP] File '%s' was already synced onces   [SYNC] New File: '%s' is synceds=   [SYNC] File '%s' already present in target, syncing tags onlyt	   copy_files$   [Conflict] File: '%s' already exists(   R�   RC   RU   RY   Ry   t   osRP   t   existst   _SyncController__sync_new_filet(   _SyncController__create_target_file_itemt    _SyncController__are_files_equalRX   t"   _SyncController__add_conflict_item(   RD   Ru   Rv   R�   R�   Rs   R�   t   target_sync_itemR~   Rt   t   files_equal(    (    s   tagstore_sync.pyt   __sync_new_item�  s&    c         C   sJ  |  j  | | | | � } | rL |  j j d | � |  j | | | | � t S| | k }	 |	 s� |  j j d | | f � | r� |  j | | | | | | � n  t S| j | � }
 t j	 |
 d � } t
 j j |  j | | � � } t j | � } t
 j j |  j | | � � } t j | � } | | k rV|  j j d | � |  j | | | | � t S| | k r�|  j j d | � t j |  j | | � |  j | | � � |  j | | | | � t S|  j | | | | � r|  j j d | � | r	|  j | | | | | | � n  t S|  j j d | � | rF|  j | | | | | | � n  t S(   s(   
        syncs an existing item
        s#   [SYNC] Tags of file '%s' are syncedsD   [Conflict] File '%s' -> %s' was added in the tagstore simultaneouslys   %Y-%m-%d %H:%M:%Ss;   [SYNC] No source modification, tags of file '%s' are synceds"   [SYNC] Updating file '%s' and tagss-   [Conflict] Both files have been modified '%s's0   [Conflict] Both files and tags are modified '%s'(   R�   RC   RU   t   _SyncController__sync_new_tagsRY   R�   RX   t   get_sync_file_timestampt   timet   strptimeR�   RP   t   getmtimeR�   t   gmtimet   shutilt   copy2t#   _SyncController__are_all_tags_equal(   RD   Ru   Rv   R�   R�   Rs   Rt   R�   R�   R�   t   str_sync_gm_timet   sync_gm_timet   mod_timet   source_gm_timet   target_gm_time(    (    s   tagstore_sync.pyt   __sync_existing_item�  sF    (c         C   sB   x; | D]3 } |  j  j d | � |  j | | | | | � q Wd S(   s!   
        starts the sync
        s   [SYNC] Current Item: %sN(   RC   RU   R�   (   RD   Ru   Rv   R�   R�   R�   Rs   (    (    s   tagstore_sync.pyt   __start_sync2  s    c   	      C   s`   | j  | � } | j  | � } | | k r. t S| j | � } | j | � } | | k r\ t St S(   sS   
        checks if all tags from the source item and target item are equal
        (   t   get_describing_tags_for_itemRX   t   get_categorizing_tags_for_itemRY   (	   RD   Ru   Rv   Rs   Rt   t   source_describing_tagst   target_describing_tagst   source_categorising_tagst   target_categorising_tags(    (    s   tagstore_sync.pyt   __are_all_tags_equal>  s    c         C   s  t  | j | � � } t  | j | � � } t  | j | � � } t  | j | � � } t  | j | � � }	 t  | j | � � }
 | | k r� |	 |
 k r� |  j j d � | j | | |
 � d S| | | B} |
 | |	 B} | j | | | � | j | | |
 � d S(   s    
        syncs new tags
        s   no changes foundN(	   t   sett!   get_describing_sync_tags_for_itemR�   t#   get_categorizing_sync_tags_for_itemR�   RC   RU   t   set_sync_tagst   add_item_with_tags(   RD   Ru   Rv   Rs   Rt   t   target_describing_sync_tagsR�   R�   t   target_categorizing_sync_tagst   target_categorizing_tagst   source_categorizing_tagst   new_describing_tagst   new_categorizing_tags(    (    s   tagstore_sync.pyt   __sync_new_tagsS  s    	c         C   sd   | j  | � } | j | � } t j |  j | | � | � | j | | | � | j | | | � d S(   s?   
        replaces the target file with the source file
        N(   R�   R�   R�   R�   R�   R�   R�   (   RD   Ru   Rv   Rs   Rt   R~   t   describing_tag_listt   categorizing_tag_list(    (    s   tagstore_sync.pyt   __sync_conflict_filey  s
    c   	      C   s   | j  | � } | j | � } | rC t j |  j | | � | � n  |  j | | � } | j | | | � | j | | | � d S(   s=   
        copies the new file and its associated tags
        N(   R�   R�   R�   R�   R�   R�   R�   R�   (	   RD   Ru   Rv   Rs   R~   R�   R�   R�   Rt   (    (    s   tagstore_sync.pyt   __sync_new_file�  s    c         C   s�   | j  d � } | d k r5 | | d t | � !} n  | j �  r� | j �  } | j �  } | t | � d t | � !d | } | j d d � } | S| Sd S(   s.   
        creates the target file name
        s   \i����i   RR   N(   t   rfindR�   R�   t   get_android_root_directoryt   get_storage_directoryRw   (   RD   Rv   Rs   t   positiont   storage_dirt   tagstore_dirt	   directory(    (    s   tagstore_sync.pyt   __create_target_file_item�  s    %c         C   sG   | j  d � } | d k r5 | | d t | � !} n  | j �  d | S(   s.   
        creates the target file path
        s   \i����i   RR   (   R�   R�   R�   (   RD   Rv   Rs   R�   (    (    s   tagstore_sync.pyt   __create_target_file_path�  s    c         C   s4   | j  �  r | j �  d | S| j �  d | Sd  S(   NRR   (   R�   R�   R�   (   RD   t   storeR|   (    (    s   tagstore_sync.pyt   __get_full_file_path�  s    c         C   s7   |  j  | | � } |  j  | | � } t j | | d � S(   s8   
        compares both files if there are equal
        i    (   R�   t   filecmpt   cmp(   RD   Ru   Rv   t   source_filet   target_filet   source_patht   target_path(    (    s   tagstore_sync.pyt   __are_files_equal�  s    c         C   s�   | j  d � } | d k r5 | | d t | � !} n  xY | D]Q } | j  d � } | d k rw | | d t | � !} n | } | | k r< | Sq< Wd S(   s�   
        finds an item which has the same name
        It is required to remove directory from the searched entries. The reasons is that 
        an Android tagstore has multiple virtual directories attached. 
        s   \i����i   N(   R�   R�   R   (   RD   t   store_itemsRs   R�   t	   file_namet   fname(    (    s   tagstore_sync.pyt   __find_item_in_store�  s    c         C   s*   |  j  j | � |  j t j d � � d  S(   Nt
   sync_error(   RC   t   errort   emitR    Rb   (   RD   t   err_msg(    (    s   tagstore_sync.pyt   __emit_not_syncable�  s    c         C   s   | |  _  d S(   s�   
        if the manager is called from another qt application (e.g. tagstore.py)
        you must set the calling application here for proper i18n
        N(   R   (   RD   RE   (    (    s   tagstore_sync.pyt   set_application�  s    c         C   s   |  j  t j d � � d S(   s4   
        the cancel button has been pressed
        t   sync_cancelN(   R  R    Rb   (   RD   (    (    s   tagstore_sync.pyt   __handle_sync_cancel  s    c         C   s  xu |  j  D]j } |  j j |  j d � � |  j j |  j d � � |  j j |  j d � � |  j j |  j d � � q
 W|  j j �  } | d k r� | |  _	 n  |  j j
 �  } | d k r� | |  _ n  |  j j �  } | d k r� | |  _ n  |  j j �  } | d k r| |  _ n  d S(   sR   
        initializes all necessary parameters for creating a store object
        R   R   t   categorizationR   RO   N(   R:   R4   R=   R2   R5   R6   R7   R   t   get_store_config_directoryR!   t   get_store_configfile_nameR#   t   get_store_tagsfile_nameR%   t   get_store_vocabularyfile_nameR'   (   RD   RK   t
   config_dirt   config_file_namet   tags_file_namet   vocabulary_file_name(    (    s   tagstore_sync.pyt   __prepare_store_params  s"    c         C   sw   |  j  j |  j � t j �  |  _ t | � } |  j j d | d d � ra |  j  j |  j � n  |  j | � |  _	 d S(   s�   
        changes the current application language
        please notice: this method is used to find all available storage/navigation directory names
        this is why it should not be extended to call any UI update methods directly
        R	   s   .qms   tsresources/N(
   R   t   removeTranslatorR/   R    R.   R*   R0   R1   R2   R3   (   RD   RJ   t   language(    (    s   tagstore_sync.pyR<   (  s    c         C   s|   |  j  j �  |  _ |  j |  j � |  j �  |  j | � |  j | � |  j j |  j	 j
 �  � |  j	 j |  j j
 �  � d S(   s/   
        initializes the store objects
        N(   R   t   get_current_languageR3   R<   t%   _SyncController__prepare_store_paramst$   _SyncController__create_source_storet$   _SyncController__create_target_storeR   t   init_sync_logR   t   get_name(   RD   Ru   Rv   (    (    s   tagstore_sync.pyt   __init_stores<  s    
c         C   s`   | j  | � } | d k r. | | k r. t Sn  | j | � } | d k r\ | | k r\ t Sn  t S(   s@   
        checks if the file has the sync tag associated
        N(   R�   R   RY   R�   RX   (   RD   Ru   Rs   R�   t   source_item_describing_tagst   source_item_categorising_tags(    (    s   tagstore_sync.pyt   __has_sync_tagT  s    c         C   sV   i  } | | d <| | d <| | d <| | d <| | d <| | d <|  j  j | � d S(   s;   
        adds a conflict item to the conflict list
        Rs   Rt   Ru   Rv   R�   R�   N(   R   R=   (   RD   Ru   Rv   R�   R�   Rs   Rt   R�   (    (    s   tagstore_sync.pyt   __add_conflict_itemi  s    





('   t   __name__t
   __module__RX   R   RM   RL   Rd   R�   R�   R{   R  R�   R  R�   R�   Rc   R�   RY   R�   R�   R�   R�   R�   R�   Rz   R�   R�   Ry   R�   R�   R�   RW   R  Re   R  R<   R�   R�   R�   (    (    (    s   tagstore_sync.pyR   $   sF   A		T				1						!		)	A			&			
	
									t   ApplicationControllerc           B   s2   e  Z d  Z d �  Z d �  Z d �  Z d �  Z RS(   s�   
    a small helper class to launch the sync-dialog as a standalone application
    this helper connects to the signals emitted by the sync controller and does the handling
    c         C   s�   t  j j |  � t j |  _ | r1 t j |  _ n  t j |  j � |  _	 t
 t j � |  _ t | | | | t � |  _ |  j |  j t  j d � |  j � |  j |  j t  j d � |  j � |  j |  j t  j d � |  j � |  j j �  d  S(   NR	  R  R�   (   R    R   R   R>   R?   R@   RA   R   RB   t   _ApplicationController__logR   R   RV   t#   _ApplicationController__main_configR   t   verbose_modet#   _ApplicationController__sync_widgetRa   Rb   t*   _ApplicationController__handle_sync_cancelt)   _ApplicationController__handle_sync_errort+   _ApplicationController__handle_sync_successRM   (   RD   RE   Ru   Rv   RH   RI   (    (    s   tagstore_sync.pyR   }  s    """c         C   s   t  j d � d S(   s7   
        exit the program if there is an error
        i����N(   t   syst   exit(   RD   (    (    s   tagstore_sync.pyt   __handle_sync_error�  s    c         C   s   t  j d � d S(   s1   
        exit the application gracefully
        i    N(   R,  R-  (   RD   (    (    s   tagstore_sync.pyt   __handle_sync_success�  s    c         C   s   t  j d � d S(   s1   
        exit the application gracefully
        i    N(   R,  R-  (   RD   (    (    s   tagstore_sync.pyR
  �  s    (   R"  R#  t   __doc__R   R*  R+  R)  (    (    (    s   tagstore_sync.pyR$  x  s
   			t   __main__sE   
This program opens a dialog used for syncing two distinct tagstores.sN   tagstore_sync.py [-source_store=<source_store>] [-target_store=<target_store>]s   -ss   --first_storet   destt   first_storet   helps/   absolute or relative path to the first tagstores   -ts   --second_storet   second_stores0   absolute or relative path to the second tagstores   -as   --auto_syncRH   R}   t
   store_trues$   automatically start the sync processs   -vs	   --verboseRI   s#   start programm with detailed outputs+   Error: source and target store are the samet   tagstore_syncs   www.tagstore.orgc           C   s   t  j �  t j �  d  S(   N(   t
   opt_parsert
   print_helpR,  R-  (    (    (    s   tagstore_sync.pyt   quit_application�  s    
(5   R,  R�   R�   R>   R�   R�   R�   t   PyQt4R    R   t   optparseR   t   tscore.loghelperR   t   tscore.configwrapperR   t   tscore.tsconstantsR   t   tscore.storeR   t   tsgui.syncdialogR   R   R   R$  R"  t   usageR8  t
   add_optiont
   parse_argst   optionst   argsR   Ru   Rv   RX   RH   R'  RI   RY   R3  R5  R9  R-  t   QApplicationt   argvt   tagstore_tagt   setApplicationNamet   setOrganizationDomaint   UnicodeUTF8t   appcontrollert   exec_R:  (    (    (    s   tagstore_sync.pyt   <module>   sj   � � � W.""						
