package com.cryptoProtocol;

import java.util.ArrayList;
import java.util.List;

public class cryptoProtocol {
    private static List<IcryptoProtocol> _cryptoProtocol = null;

    public static List<IcryptoProtocol> getMenuItems()
    {
        if (_cryptoProtocol == null)
        {
            _cryptoProtocol = new ArrayList<IcryptoProtocol>();
            _cryptoProtocol.add(new KeyExchange());
            _cryptoProtocol.add(new Yahalom());
            _cryptoProtocol.add(new DenningSacco());
            _cryptoProtocol.add(new ConferenceCalls());
        }
        return _cryptoProtocol;
    }

}
