³ò
W$ÌLc           @   se  d  d k  Z  d  d k Z d  d k l Z d  d k l Z l Z d  d k l	 Z	 d  d k
 l Z d  d k l Z d  d k l Z d e i f d	     YZ e d
 j o½ e d  Z e i d d d d d d d d e i   \ Z Z e Z e Z e i o
 e Z n e i e  i  Z e i  d  e i! d  e i" e d e  Z# e# i$ e  e i%   n d S(   iÿÿÿÿN(   t   OptionParser(   t   QtCoret   QtGui(   t   Store(   t   ConfigWrapper(   t   TsConstants(   t   StorePreferencesControllert   Administrationc           B   sY   e  Z d    Z d   Z d   Z d   Z d   Z d   Z d   Z d   Z	 d   Z
 RS(	   c         C   sð   t  i i |   d  |  _ d  |  _ d  |  _ t i |  _	 | o t i
 |  _	 n t i |  _ t i |  _ t i |  _ t i |  _ |  i d  |  _ |  i } g  |  _ g  |  _ g  |  _ g  |  _ t i |  _ h  |  _ |  i |  i	  |  i   d  S(   Nt   en(   R   t   QObjectt   __init__t   Nonet   _Administration__logt   _Administration__main_configt   _Administration__admin_dialogt   loggingt   INFOt	   LOG_LEVELt   DEBUGR   t   DEFAULT_STORE_CONFIG_DIRt   STORE_CONFIG_DIRt   DEFAULT_STORE_CONFIG_FILENAMEt   STORE_CONFIG_FILE_NAMEt   DEFAULT_STORE_TAGS_FILENAMEt   STORE_TAGS_FILE_NAMEt!   DEFAULT_STORE_VOCABULARY_FILENAMEt   STORE_VOCABULARY_FILE_NAMEt   trUtf8t   CURRENT_LANGUAGEt   STORE_STORAGE_DIRSt   STORE_DESCRIBING_NAV_DIRSt   STORE_CATEGORIZING_NAV_DIRSt   STORE_EXPIRED_DIRSt   DEFAULT_SUPPORTED_LANGUAGESt   SUPPORTED_LANGUAGESt   _Administration__store_dictt   _Administration__init_loggert#   _Administration__init_configuration(   t   selft   verboset   store_current_language(    (    sB   /home/vk/src/tagstore/research_platform/tagstore/administration.pyR
      s*    									c         C   s»   t  i } t i t  i  |  _ |  i i t i  t i d  } t i	   } | i |  | i
 |  t i i | d t  i d t  i } | i
 |  |  i i |  |  i i |  d  S(   Ns)   %(asctime)s - %(levelname)s - %(message)st   maxBytest   backupCount(   R   t   LOG_FILENAMER   t	   getLoggert   LOGGER_NAMER   t   setLevelR   t	   Formattert   StreamHandlert   setFormattert   handlerst   RotatingFileHandlert   LOG_FILESIZEt   LOG_BACKUP_COUNTt
   addHandler(   R&   t	   log_levelR+   t	   formattert   console_handlert   file_handler(    (    sB   /home/vk/src/tagstore/research_platform/tagstore/administration.pyt   __init_logger;   s    	c         C   sW  |  i  i d  t t i  |  _ |  i d	 j ov t   |  _ |  i	 |  i t
 i d  |  i  |  i	 |  i t
 i d  |  i  |  i	 |  i t
 i d  |  i  n |  i i |  i  |  i   |  i   g  } xk |  i i   D]Z } | d i d  i   } |  i | i   | d <|  i | i   | d <| i |  qå W|  i i |  d	 S(
   si   
        initializes the configuration. This method is called every time the config file changes
        s   initialize configurationt   create_new_storet   rename_desc_tagt   rename_cat_tagt   patht   /t	   desc_tagst   cat_tagsN(   R   t   infoR   R   t   CONFIG_PATHR   R   R   R   t   connectR   t   SIGNALt!   _Administration__handle_new_storet"   _Administration__handle_tag_renamet   set_main_configt%   _Administration__prepare_store_paramst   _Administration__create_storest
   get_storest   splitt   popR#   t   get_tagst   get_categorizing_tagst   appendt   set_store_list(   R&   t   tmp_store_listt   current_store_itemt
   store_name(    (    sB   /home/vk/src/tagstore/research_platform/tagstore/administration.pyt   __init_configurationQ   s$    ""&

 c         C   s-   |  i  | } | i t |  t |   d  S(   N(   R#   t
   rename_tagt   str(   R&   t   old_tagt   new_tagRU   t   store(    (    sB   /home/vk/src/tagstore/research_platform/tagstore/administration.pyt   __handle_tag_renamen   s    c         C   s   |  i  i   d  S(   N(   R   t   show_dialog(   R&   t   show(    (    sB   /home/vk/src/tagstore/research_platform/tagstore/administration.pyt   show_admin_dialogr   s    c         C   s   |  i  i |  d S(   sV   
        set the parent for the admin-dialog if there is already a gui window
        N(   R   t
   set_parent(   R&   t   parent(    (    sB   /home/vk/src/tagstore/research_platform/tagstore/administration.pyR`   u   s    c         C   s   xu |  i  D]j } |  i i |  i d   |  i i |  i d   |  i i |  i d   |  i i |  i d   q
 W|  i i   } | d j o | |  _	 n |  i i
   } | d j o | |  _ n |  i i   } | d j o | |  _ n |  i i   } | d j o | |  _ n d S(   sM   
        initialzes all necessary params for creating a store object
        t   storaget
   navigationt   categorizationt   expired_itemst    N(   R"   R   RQ   R   R   R   R    R   t   get_store_config_directoryR   t   get_store_configfile_nameR   t   get_store_tagsfile_nameR   t   get_store_vocabularyfile_nameR   (   R&   t   langt
   config_dirt   config_file_namet   tags_file_namet   vocabulary_file_name(    (    sB   /home/vk/src/tagstore/research_platform/tagstore/administration.pyt   __prepare_store_params{   s$    
 c         C   sÂ   |  i  i   } x¬ | D]¤ } | d i d  i   } t | d | d |  i d |  i |  i d |  i |  i d |  i |  i	 |  i
 |  i |  i |  i  i   
 } | i   | |  i | <q Wd  S(   NR?   R@   t   id(   R   RL   RM   RN   R   R   R   R   R   R   R   R   R    t   get_expiry_prefixt   initR#   (   R&   t   store_itemsRT   RU   t	   tmp_store(    (    sB   /home/vk/src/tagstore/research_platform/tagstore/administration.pyt   __create_stores   s     
c         C   s   |  i  i |  } t | | |  i d |  i |  i d |  i |  i d |  i |  i |  i |  i	 |  i
 |  i  i   
 } | i   |  i   d S(   s5   
        create new store at given directory
        R@   N(   R   t   add_new_storeR   R   R   R   R   R   R   R   R    Rr   Rs   R%   (   R&   t   dirt   store_idRu   (    (    sB   /home/vk/src/tagstore/research_platform/tagstore/administration.pyt   __handle_new_store¨   s    	
(   t   __name__t
   __module__R
   R$   R%   RH   R_   R`   RJ   RK   RG   (    (    (    sB   /home/vk/src/tagstore/research_platform/tagstore/administration.pyR      s   								t   __main__s   tagstore_admin.py [options]s   -vs	   --verboset   destR'   t   actiont
   store_truet   helps#   start programm with detailed outputt   tagstore_admins   www.tagstore_admin.org(&   t   syst   logging.handlersR   t   optparseR    t   PyQt4R   R   t   tscore.storeR   t   tscore.configwrapperR   t   tscore.tsconstantsR   t   tsgui.admindialogR   R	   R   R{   t
   opt_parsert
   add_optiont
   parse_argst   optionst   argst   Falset   verbose_modet   dry_runR'   t   Truet   QApplicationt   argvR   t   setApplicationNamet   setOrganizationDomaint   UnicodeUTF8t   admin_widgetR_   t   exec_(    (    (    sB   /home/vk/src/tagstore/research_platform/tagstore/administration.pys   <module>   s.   ¡"

