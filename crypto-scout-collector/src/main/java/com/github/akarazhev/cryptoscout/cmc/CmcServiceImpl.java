package com.github.akarazhev.cryptoscout.cmc;

import com.github.akarazhev.jcryptolib.stream.Payload;
import com.github.akarazhev.jcryptolib.stream.Provider;
import com.github.akarazhev.jcryptolib.stream.Source;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.github.akarazhev.jcryptolib.cmc.Constants.Response.BTC_PRICE;
import static com.github.akarazhev.jcryptolib.cmc.Constants.Response.BTC_VOLUME;
import static com.github.akarazhev.jcryptolib.cmc.Constants.Response.DATA_LIST;
import static com.github.akarazhev.jcryptolib.cmc.Constants.Response.NAME;
import static com.github.akarazhev.jcryptolib.cmc.Constants.Response.SCORE;
import static com.github.akarazhev.jcryptolib.cmc.Constants.Response.TIMESTAMP;

@Service
class CmcServiceImpl implements CmcService {
    private final CmcFgiRepository cmcFgiRepository;

    public CmcServiceImpl(final CmcFgiRepository cmcFgiRepository) {
        this.cmcFgiRepository = cmcFgiRepository;
    }

    @Transactional
    @Override
    public void save(final Payload<Map<String, Object>> payload) {
        final var provider = payload.getProvider();
        final var source = payload.getSource();
        if (Provider.CMC.equals(provider)) {
            if (Source.FGI.equals(source)) {
                cmcFgiRepository.saveAll(getCmcFgis(payload.getData()));
            }
        }
    }

    private Collection<CmcFgi> getCmcFgis(final Map<String, Object> data) {
        final var dataList = (List<Map<String, Object>>) data.get(DATA_LIST);
        final var cmcFgis = new ArrayList<CmcFgi>();
        for (final var point : dataList) {
            final var cmcFgi = new CmcFgi();
            if (point.get(SCORE) != null) {
                cmcFgi.setScore((Integer) point.get(SCORE));
            }

            if (point.get(NAME) != null) {
                cmcFgi.setName((String) point.get(NAME));
            }

            if (point.get(TIMESTAMP) != null) {
                cmcFgi.setTimestamp(Instant.ofEpochSecond(Long.parseLong((String) point.get(TIMESTAMP))));
            }

            if (point.get(BTC_PRICE) != null) {
                cmcFgi.setBtcPrice(BigDecimal.valueOf(Double.parseDouble((String) point.get(BTC_PRICE))));
            }

            if (point.get(BTC_VOLUME) != null) {
                cmcFgi.setBtcVolume(BigDecimal.valueOf(Double.parseDouble((String) point.get(BTC_VOLUME))));
            }

            cmcFgis.add(cmcFgi);
        }

        return cmcFgis;
    }
}
