package com.pulapirata.core;

import react.IntValue;

/**
 * A class that just partitions values into qualitative states.
 * For instance: Alcool: drunk, sober, hangover etc.
 */
class PetAttributeState extends IntValue {
    public PetAttributeState(PetAttribute attr, statelist, intervals) {
        // TODO make sure supplied intervals partitions the range of that
        // parameter

        /* hook qualitative attributes to reduce ifs - make logic more
         * declarative */
        attr_ = attr;
        attr_.connect(slot());
    }

    public PetAttribute attr_;
}