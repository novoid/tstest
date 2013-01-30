


#ifndef __TSINGELTON_HOLDER_H__
#define __TSINGELTON_HOLDER_H__



template <class T>
class SingeltonHolder
{

private:

	static T * m_pInstance;

public:

	static T * Instance()
	{
		if (!m_pInstance)
			m_pInstance = new T;

		return m_pInstance;
	}
};

template <class T>
T * SingeltonHolder<T>::m_pInstance = 0;


#endif