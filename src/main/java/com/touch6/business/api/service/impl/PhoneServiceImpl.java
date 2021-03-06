package com.touch6.business.api.service.impl;

import com.touch6.business.api.service.PhoneService;
import com.touch6.constant.SmsGatewayConstant;
import com.touch6.core.exception.CoreException;
import com.touch6.core.exception.ECodeUtil;
import com.touch6.core.exception.error.constant.PhoneErrorConstant;
import com.touch6.business.mybatis.PhoneCodeMybatisDao;
import com.touch6.business.mybatis.UserMybatisDao;
import com.touch6.business.enums.PhoneVerifyResult;
import com.touch6.business.enums.SmsGatewayInterface;
import com.touch6.business.entity.PhoneCode;
import com.touch6.sm.gateway.Touch6SmsUtil;
import com.touch6.utils.T6DateUtils;
import com.touch6.utils.T6PropertiesUtil;
import com.touch6.utils.T6StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/*
 * ============================================================================		
 * = COPYRIGHT		
 *               PAX TECHNOLOGY, Inc. PROPRIETARY INFORMATION		
 *   This software is supplied under the terms of a license agreement or		
 *   nondisclosure agreement with PAX  Technology, Inc. and may not be copied		
 *   or disclosed except in accordance with the terms in that agreement.		
 *      Copyright (C) 2017-? PAX Technology, Inc. All rights reserved.		
 * Description: // Detail description about the function of this module,		
 *             // interfaces with the other modules, and dependencies. 		
 * Revision History:		
 * Date	                 Author	                  Action
 * 2017/2/23  	         zhuxl@paxsz.com        Create/Add/Modify/Delete
 * ============================================================================		
 */
@SuppressWarnings("ALL")
@Service
public class PhoneServiceImpl implements PhoneService {
    private static final Logger logger = LoggerFactory.getLogger(PhoneServiceImpl.class);

    @Autowired
    UserMybatisDao userMybatisDao;
    @Autowired
    PhoneCodeMybatisDao phoneCodeMybatisDao;

    @Override
    public void checkPhone(String phone) throws CoreException {
        String mbl = userMybatisDao.checkPhone(phone);
        if (org.apache.commons.lang.StringUtils.isNotBlank(mbl)) {
            throw new CoreException(ECodeUtil.getCommError(PhoneErrorConstant.PHONE_ALREADY_REGISTERED));
        }
    }

    @Override
    @Transactional
    public void generatePhoneCode(String phone) throws CoreException {
        String code = T6StringUtils.generate6PhoneCode();
        Date now = T6DateUtils.nowTime();
        String selectedGateway = T6PropertiesUtil.getValue(SmsGatewayConstant.SMS_GATEWAY_PROPERTIES_FILENAME, SmsGatewayConstant.SMS_GATEWAY_SELECTED);
        SmsGatewayInterface gateway = SmsGatewayInterface.valueOf(selectedGateway);

        PhoneCode phoneCode = phoneCodeMybatisDao.findByPhone(phone);
        if (phoneCode == null) {
            //该手机号未生成过验证码
            phoneCode = new PhoneCode();
            phoneCode.setId(T6StringUtils.generate32uuid());
            phoneCode.setPhone(phone);
            phoneCode.setPresCode(code);
            phoneCode.setTimes(1);
            phoneCode.setVerifyTimes(0);
            Date time = T6DateUtils.nowTime();
            phoneCode.setPresTime(time);
            //insert phoneCode
            phoneCodeMybatisDao.insertPhoneCode(phoneCode);
        } else {
            //该手机号生成过验证码，再次生成
            if (1 * 60 * 1000 > (now.getTime() - phoneCode.getPresTime().getTime())) {
                //发送验证码1分钟只能点击发送1次；
                throw new CoreException(ECodeUtil.getCommError(PhoneErrorConstant.PHONE_CODE_TOO_FREQUENT_60));
            }
            //累加次数
            phoneCode.setTimes(phoneCode.getTimes() + 1);

            phoneCode.setPrevTime(phoneCode.getPresTime());
            phoneCode.setPresTime(now);
            phoneCode.setPrevCode(phoneCode.getPresCode());
            phoneCode.setPresCode(code);
            phoneCode.setVerifyTimes(0);
            phoneCode.setPrevVerifyResult(phoneCode.getPresVerifyResult());
            //update phoneCode
            phoneCodeMybatisDao.updatePhoneCode(phoneCode);
        }
        logger.info("向手机号：[{}]发送验证码：[{}]",phone,code);
        Touch6SmsUtil.sendSmsCode(gateway, phone, code);
    }

    @Override
    @Transactional
    public void verifyPhoneCode(String phone, String code) throws CoreException {
        PhoneCode phoneCode = phoneCodeMybatisDao.findByPhone(phone);
        if (phoneCode == null) {
            //手机号错误
            throw new CoreException(ECodeUtil.getCommError(PhoneErrorConstant.PHONE_INCORRECT));
        }
        //判定时间是否在expired分钟内
        Date now = T6DateUtils.nowTime();
        long expired = Long.parseLong(T6PropertiesUtil.getValue(SmsGatewayConstant.SMS_GATEWAY_PROPERTIES_FILENAME, SmsGatewayConstant.SMS_CODE_EXPIRED));
        if (expired * 60 * 1000 < (now.getTime() - phoneCode.getPresTime().getTime())) {
            throw new CoreException(ECodeUtil.getCommError(PhoneErrorConstant.PHONE_CODE_EXPIRED));
        }
        if (phoneCode.getPresCode().equals(code)) {
            //equals
            //只允许验证1次,验证1次后失效
            if (phoneCode.getVerifyTimes() == 1) {
                throw new CoreException(ECodeUtil.getCommError(PhoneErrorConstant.PHONE_CODE_ONLY_VERIFY_ONE_TIMES));
            }
            phoneCode.setVerifyTimes(1);
            phoneCode.setPrevVerifyResult(phoneCode.getPresVerifyResult());
            phoneCode.setPresVerifyResult(PhoneVerifyResult.SUCCESS);
            //update phoneCode
            phoneCodeMybatisDao.updatePhoneCode(phoneCode);
        } else {
            throw new CoreException(ECodeUtil.getCommError(PhoneErrorConstant.PHONE_CODE_INCORRECT));
        }
    }
}
