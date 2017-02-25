package com.qingsb.sm.gateway;

import com.qingsb.core.exception.CoreException;
import com.qingsb.enums.SmsGatewayInterface;

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
 * 2017/2/25  	         zhuxl@paxsz.com        Create/Add/Modify/Delete
 * ============================================================================		
 */
public class QingsbTest {
    public static void main(String[] args) throws CoreException {
        QingsbSmsUtil.sendSmsCode(SmsGatewayInterface.ALIYUN,"13880298929","266399");
    }
}
